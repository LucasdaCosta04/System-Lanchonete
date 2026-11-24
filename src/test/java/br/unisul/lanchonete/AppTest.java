package br.unisul.lanchonete;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.unisul.lanchonete.web.ServidorWeb;

class AppTest {

    // Executa o roteiro imprimindo num buffer em memória e devolve o texto impresso.
    private String executarRoteiro() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new App(new PrintStream(buffer, true, StandardCharsets.UTF_8)).executar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    // Junta as linhas com a quebra de linha do sistema, igual ao println.
    private String linhas(String... linhas) {
        return String.join(System.lineSeparator(), linhas);
    }

    @Test
    void mainDeveExecutarORoteiroNoConsoleSemPedirEntrada() {
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        try {
            App.main(new String[] {});
        } finally {
            System.setOut(saidaOriginal);
        }

        String saida = buffer.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(saida.startsWith("=== Sistema de Lanchonete ===")),
                () -> assertTrue(saida.contains("=== Fim da demonstração ===")));
    }

    @Test
    void deveCadastrarClienteEProdutosComEstoqueInicial() {
        String saida = executarRoteiro();

        assertAll(
                () -> assertTrue(saida.contains("Cliente cadastrado: Maria Silva (id 1)")),
                () -> assertTrue(saida.contains("Produto cadastrado: X-Burguer | R$ 18,00")),
                () -> assertTrue(saida.contains(linhas(
                        "Estoque inicial:",
                        "  X-Burguer: 10",
                        "  Refrigerante: 5",
                        "  Batata Frita: 3"))));
    }

    @Test
    void deveConfirmarPedido1ExibindoTotalEBaixaNoEstoque() {
        String saida = executarRoteiro();

        assertAll(
                () -> assertTrue(saida.contains("  + 2 x X-Burguer (R$ 18,00 cada)")),
                () -> assertTrue(saida.contains("Pedido 1 confirmado (status CONFIRMADO)")),
                // 2 x 18,00 + 2 x 6,00 + 1 x 9,50
                () -> assertTrue(saida.contains("Total do pedido: R$ 57,50")),
                () -> assertTrue(saida.contains(linhas(
                        "Estoque após a baixa automática:",
                        "  X-Burguer: 8",
                        "  Refrigerante: 3",
                        "  Batata Frita: 2"))));
    }

    @Test
    void deveRecusarPedido2SemBaixaParcialMantendoPedidoAberto() {
        String saida = executarRoteiro();

        assertAll(
                () -> assertTrue(saida.contains(
                        "Confirmação recusada: Pedido não confirmado: estoque insuficiente para o produto 'Batata Frita'.")),
                () -> assertTrue(saida.contains("Pedido 2 segue com status ABERTO")),
                // O X-Burguer do Pedido 2 tinha estoque, mas continua em 8: nenhuma baixa parcial.
                () -> assertTrue(saida.contains(linhas(
                        "Estoque intacto (nenhuma baixa parcial):",
                        "  X-Burguer: 8",
                        "  Refrigerante: 3",
                        "  Batata Frita: 2"))));
    }

    @Test
    void deveCancelarPedido1DevolvendoOsItensAoEstoque() {
        String saida = executarRoteiro();

        assertAll(
                () -> assertTrue(saida.contains("Pedido 1 cancelado (status CANCELADO)")),
                () -> assertTrue(saida.contains(linhas(
                        "Estoque após a reposição automática:",
                        "  X-Burguer: 10",
                        "  Refrigerante: 5",
                        "  Batata Frita: 3"))));
    }

    // ---------- Modo web (--web) ----------

    private static HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpClient cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        return cliente.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void lerPortaSemArgumentoDeveUsarAPortaPadrao() {
        assertEquals(8080, App.lerPorta(new String[] { "--web" }));
    }

    @Test
    void lerPortaDeveUsarOArgumentoPorta() {
        assertEquals(9090, App.lerPorta(new String[] { "--web", "--porta=9090" }));
        assertEquals(0, App.lerPorta(new String[] { "--porta=0", "--web" }));
    }

    @ParameterizedTest
    @ValueSource(strings = { "abc", "", "-1", "65536", "8080.5" })
    void lerPortaInvalidaDeveLancarExcecaoComMensagemClara(String porta) {
        String[] args = { "--web", "--porta=" + porta };

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> App.lerPorta(args));

        assertEquals("Porta inválida: '" + porta + "'. Use um número entre 0 e 65535.", erro.getMessage());
    }

    @Test
    void iniciarWebDeveSubirServidorComOCenarioBase() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ServidorWeb servidor = new App(new PrintStream(buffer, true, StandardCharsets.UTF_8)).iniciarWeb(0);

        try {
            String saida = buffer.toString(StandardCharsets.UTF_8);
            String clientes = get(servidor.getEndereco() + "/api/clientes").body();
            String produtos = get(servidor.getEndereco() + "/api/produtos").body();

            assertAll(
                    () -> assertTrue(saida.startsWith("=== Sistema de Lanchonete (modo web) ===")),
                    () -> assertTrue(saida.contains("Cliente cadastrado: Maria Silva (id 1)")),
                    () -> assertTrue(saida.contains("Interface web disponível em " + servidor.getEndereco())),
                    () -> assertTrue(clientes.contains("\"nome\":\"Maria Silva\"")),
                    () -> assertTrue(produtos.contains("\"nome\":\"X-Burguer\"")),
                    () -> assertTrue(produtos.contains("\"nome\":\"Refrigerante\"")),
                    () -> assertTrue(produtos.contains("\"nome\":\"Batata Frita\"")));
        } finally {
            servidor.parar();
        }
    }

    // O main não devolve o servidor; ele fica no ar até a JVM dos testes terminar,
    // quando o shutdown hook registrado pelo próprio main o encerra.
    @Test
    void mainComWebDeveAbrirAInterfaceNoEnderecoImpresso() throws Exception {
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        try {
            App.main(new String[] { "--web", "--porta=0" });
        } finally {
            System.setOut(saidaOriginal);
        }

        Matcher endereco = Pattern.compile("Interface web disponível em (http://127\\.0\\.0\\.1:\\d+)")
                .matcher(buffer.toString(StandardCharsets.UTF_8));
        assertTrue(endereco.find());

        HttpResponse<String> pagina = get(endereco.group(1) + "/");
        assertEquals(200, pagina.statusCode());
        assertTrue(pagina.body().contains("<title>Sistema de Lanchonete</title>"));
    }
}
