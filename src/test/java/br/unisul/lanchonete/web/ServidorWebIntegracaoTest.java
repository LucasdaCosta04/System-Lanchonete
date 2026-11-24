package br.unisul.lanchonete.web;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.repository.ClienteRepositoryEmMemoria;
import br.unisul.lanchonete.repository.PedidoRepositoryEmMemoria;
import br.unisul.lanchonete.repository.ProdutoRepositoryEmMemoria;
import br.unisul.lanchonete.service.ClienteService;
import br.unisul.lanchonete.service.EstoqueService;
import br.unisul.lanchonete.service.PedidoService;
import br.unisul.lanchonete.service.ProdutoService;

/**
 * Sobe o servidor de verdade (porta livre, em 127.0.0.1) com serviços e repositórios
 * em memória reais e faz requisições HTTP com o HttpClient do próprio Java.
 * Produtos cadastrados: 1 = X-Burguer (estoque 10), 2 = Batata Frita (estoque 3).
 */
@Tag("integracao")
@DisplayName("Integração: ServidorWeb + PedidoApi + serviços em memória")
class ServidorWebIntegracaoTest {

    private final HttpClient cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private PedidoApi api;
    private ServidorWeb servidor;

    @BeforeEach
    void setUp() {
        ClienteRepositoryEmMemoria clienteRepository = new ClienteRepositoryEmMemoria();
        ProdutoRepositoryEmMemoria produtoRepository = new ProdutoRepositoryEmMemoria();
        ClienteService clienteService = new ClienteService(clienteRepository);
        ProdutoService produtoService = new ProdutoService(produtoRepository);
        PedidoService pedidoService = new PedidoService(new PedidoRepositoryEmMemoria(), clienteRepository,
                produtoRepository, new EstoqueService(produtoRepository));

        clienteService.cadastrar(new Cliente("Maria Silva", "12345678909", "(48) 99999-0000"));
        produtoService.cadastrar(new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10));
        produtoService.cadastrar(new Produto("Batata Frita", "Porção", new BigDecimal("9.50"), 3));

        api = new PedidoApi(pedidoService, clienteService, produtoService);
        servidor = new ServidorWeb(api, 0);
        servidor.iniciar();
    }

    @AfterEach
    void tearDown() {
        servidor.parar();
    }

    private HttpResponse<String> enviar(HttpRequest.Builder requisicao) throws IOException, InterruptedException {
        return cliente.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String caminho) throws IOException, InterruptedException {
        return enviar(HttpRequest.newBuilder(URI.create(servidor.getEndereco() + caminho)).GET());
    }

    private HttpResponse<String> post(String caminho, String corpo) throws IOException, InterruptedException {
        return enviar(HttpRequest.newBuilder(URI.create(servidor.getEndereco() + caminho))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(corpo)));
    }

    @Test
    void deveEscutarEmPortaLivreNoEnderecoLocal() {
        assertTrue(servidor.getPorta() > 0);
        assertEquals("http://127.0.0.1:" + servidor.getPorta(), servidor.getEndereco());
    }

    @Test
    void raizDeveEntregarAPaginaHtml() throws Exception {
        HttpResponse<String> resposta = get("/");

        assertAll(
                () -> assertEquals(200, resposta.statusCode()),
                () -> assertEquals("text/html; charset=utf-8", resposta.headers().firstValue("Content-Type").orElse("")),
                () -> assertTrue(resposta.body().contains("<title>Sistema de Lanchonete</title>")));
    }

    @Test
    void arquivoForaDaListaDeveRetornar404() throws Exception {
        assertEquals(404, get("/pom.xml").statusCode());
    }

    @Test
    void postNaPaginaDeveRetornar405() throws Exception {
        assertEquals(405, post("/", "").statusCode());
    }

    @Test
    void apiDeveListarClientesEProdutosEmJson() throws Exception {
        HttpResponse<String> clientes = get("/api/clientes");
        HttpResponse<String> produtos = get("/api/produtos");

        assertAll(
                () -> assertEquals(200, clientes.statusCode()),
                () -> assertEquals("application/json; charset=utf-8",
                        clientes.headers().firstValue("Content-Type").orElse("")),
                () -> assertTrue(clientes.body().contains("\"nome\":\"Maria Silva\"")),
                () -> assertTrue(produtos.body().contains("\"nome\":\"X-Burguer\"")),
                () -> assertTrue(produtos.body().contains("\"nome\":\"Batata Frita\"")));
    }

    @Test
    void fluxoCompletoDeveBaixarEReporEstoque() throws Exception {
        HttpResponse<String> criado = post("/api/pedidos", "clienteId=1");
        HttpResponse<String> comItem = post("/api/pedidos/1/itens", "produtoId=1&quantidade=2");
        HttpResponse<String> confirmado = post("/api/pedidos/1/confirmar", "");
        String estoqueAposConfirmar = get("/api/produtos").body();
        HttpResponse<String> cancelado = post("/api/pedidos/1/cancelar", "");
        String estoqueAposCancelar = get("/api/produtos").body();

        assertAll(
                () -> assertEquals(201, criado.statusCode()),
                () -> assertTrue(criado.body().contains("\"status\":\"ABERTO\"")),
                () -> assertEquals(200, comItem.statusCode()),
                () -> assertTrue(comItem.body().contains("\"total\":36.00")),
                () -> assertEquals(200, confirmado.statusCode()),
                () -> assertTrue(confirmado.body().contains("\"status\":\"CONFIRMADO\"")),
                () -> assertTrue(estoqueAposConfirmar.contains("\"nome\":\"X-Burguer\",\"categoria\":\"Lanche\","
                        + "\"preco\":18.00,\"estoque\":8")),
                () -> assertEquals(200, cancelado.statusCode()),
                () -> assertTrue(cancelado.body().contains("\"status\":\"CANCELADO\"")),
                () -> assertTrue(estoqueAposCancelar.contains("\"preco\":18.00,\"estoque\":10")));
    }

    @Test
    void confirmacaoSemEstoqueDeveRetornar409SemBaixaParcial() throws Exception {
        post("/api/pedidos", "clienteId=1");
        post("/api/pedidos/1/itens", "produtoId=1&quantidade=1");
        post("/api/pedidos/1/itens", "produtoId=2&quantidade=5");

        HttpResponse<String> recusado = post("/api/pedidos/1/confirmar", "");
        String produtos = get("/api/produtos").body();
        String pedidos = get("/api/pedidos").body();

        assertAll(
                () -> assertEquals(409, recusado.statusCode()),
                () -> assertTrue(recusado.body().contains("estoque insuficiente para o produto 'Batata Frita'")),
                () -> assertTrue(produtos.contains("\"preco\":18.00,\"estoque\":10")),
                () -> assertTrue(produtos.contains("\"preco\":9.50,\"estoque\":3")),
                () -> assertTrue(pedidos.contains("\"status\":\"ABERTO\"")));
    }

    @Test
    void errosDevemVirarCodigosHttp() throws Exception {
        post("/api/pedidos", "clienteId=1");
        post("/api/pedidos/1/cancelar", "");

        assertAll(
                () -> assertEquals(409, post("/api/pedidos/1/cancelar", "").statusCode()),
                () -> assertEquals(400, post("/api/pedidos/99/confirmar", "").statusCode()),
                () -> assertEquals(400, post("/api/pedidos", "clienteId=abc").statusCode()),
                () -> assertEquals(404, post("/api/pedidos", "clienteId=99").statusCode()),
                () -> assertEquals(404, get("/api/inexistente").statusCode()),
                () -> assertEquals(405, enviar(HttpRequest.newBuilder(
                        URI.create(servidor.getEndereco() + "/api/pedidos")).DELETE()).statusCode()));
    }

    @Test
    void portaJaEmUsoDeveLancarExcecaoComMensagemClara() {
        int portaOcupada = servidor.getPorta();

        UncheckedIOException erro = assertThrows(UncheckedIOException.class, () -> new ServidorWeb(api, portaOcupada));

        assertEquals("Não foi possível abrir o servidor na porta " + portaOcupada + ".", erro.getMessage());
    }

    @Test
    void construtorDeveExigirApi() {
        assertThrows(NullPointerException.class, () -> new ServidorWeb(null, 0));
    }
}
