package br.unisul.lanchonete;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

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
}
