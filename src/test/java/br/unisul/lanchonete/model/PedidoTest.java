package br.unisul.lanchonete.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Os valores BigDecimal são comparados com compareTo, para que a escala
 * (ex.: 37.5 e 37.50) não influencie o resultado.
 */
class PedidoTest {

    @Test
    void deveIniciarComStatusAberto() {
        Pedido pedido = new Pedido(1L);

        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
        assertEquals(1L, pedido.getClienteId());
    }

    @Test
    void deveTerTotalZeroQuandoNaoHouverItens() {
        Pedido pedido = new Pedido(1L);

        assertTrue(pedido.getItens().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getTotal()));
    }

    @Test
    void deveTerTotalIgualAoSubtotalQuandoHouverUmItem() {
        Pedido pedido = new Pedido(1L);
        pedido.adicionarItem(new ItemPedido(10L, "X-Salada", 3, new BigDecimal("12.50")));

        assertEquals(1, pedido.getItens().size());
        assertEquals(0, new BigDecimal("37.50").compareTo(pedido.getTotal()));
    }

    @Test
    void deveCalcularTotalComoSomaDeQuantidadeVezesPrecoUnitario() {
        Pedido pedido = new Pedido(1L);
        pedido.adicionarItem(new ItemPedido(10L, "X-Salada", 3, new BigDecimal("12.50")));   // 37.50
        pedido.adicionarItem(new ItemPedido(20L, "Refrigerante", 2, new BigDecimal("8.90"))); // 17.80
        pedido.adicionarItem(new ItemPedido(30L, "Batata frita", 1, new BigDecimal("15")));  // 15.00

        assertEquals(3, pedido.getItens().size());
        assertEquals(0, new BigDecimal("70.30").compareTo(pedido.getTotal()));
    }

    @Test
    void deveLancarErroAoAdicionarItemNulo() {
        Pedido pedido = new Pedido(1L);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> pedido.adicionarItem(null));

        assertEquals("O item do pedido é obrigatório.", erro.getMessage());
        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    void naoDevePermitirAlterarAListaDeItensDiretamente() {
        Pedido pedido = new Pedido(1L);
        pedido.adicionarItem(new ItemPedido(10L, "X-Salada", 1, new BigDecimal("12.50")));
        List<ItemPedido> itens = pedido.getItens();
        ItemPedido outroItem = new ItemPedido(20L, "Refrigerante", 1, new BigDecimal("8.90"));

        assertThrows(UnsupportedOperationException.class, () -> itens.add(outroItem));
        assertThrows(UnsupportedOperationException.class, () -> itens.remove(0));
        assertEquals(1, pedido.getItens().size());
    }

    @Test
    void devePermitirAlterarIdEStatus() {
        Pedido pedido = new Pedido(1L);

        pedido.setId(5L);
        pedido.setStatus(StatusPedido.CONFIRMADO);

        assertEquals(5L, pedido.getId());
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
    }

    // ---------- Issue #18: transições de status ----------

    // Cria um pedido com um item (12.50) já no status desejado, seguindo o fluxo válido.
    private Pedido pedidoComUmItemNoStatus(StatusPedido status) {
        Pedido pedido = new Pedido(1L);
        pedido.adicionarItem(new ItemPedido(10L, "X-Salada", 1, new BigDecimal("12.50")));
        if (status != StatusPedido.ABERTO) {
            pedido.setStatus(status);
        }
        return pedido;
    }

    @Test
    void deveLancarErroAoDefinirStatusNulo() {
        Pedido pedido = new Pedido(1L);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> pedido.setStatus(null));

        assertEquals("O status do pedido não pode ser nulo.", erro.getMessage());
        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
    }

    @Test
    void devePermitirCancelarPedidoAberto() {
        Pedido pedido = new Pedido(1L);

        pedido.setStatus(StatusPedido.CANCELADO);

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    void naoDevePermitirAbertoParaAberto() {
        Pedido pedido = new Pedido(1L);

        assertThrows(IllegalStateException.class, () -> pedido.setStatus(StatusPedido.ABERTO));
        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
    }

    @Test
    void devePermitirCancelarPedidoConfirmado() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CONFIRMADO);

        pedido.setStatus(StatusPedido.CANCELADO);

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    void naoDevePermitirConfirmadoParaAberto() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CONFIRMADO);

        assertThrows(IllegalStateException.class, () -> pedido.setStatus(StatusPedido.ABERTO));
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
    }

    @Test
    void naoDevePermitirConfirmarPedidoJaConfirmado() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CONFIRMADO);

        assertThrows(IllegalStateException.class, () -> pedido.setStatus(StatusPedido.CONFIRMADO));
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
    }

    @Test
    void naoDevePermitirConfirmarPedidoCancelado() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CANCELADO);

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> pedido.setStatus(StatusPedido.CONFIRMADO));

        assertEquals("Um pedido cancelado não pode ter o status alterado.", erro.getMessage());
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    void naoDevePermitirCancelarPedidoJaCancelado() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CANCELADO);

        assertThrows(IllegalStateException.class, () -> pedido.setStatus(StatusPedido.CANCELADO));
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    void naoDevePermitirReabrirPedidoCancelado() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CANCELADO);

        assertThrows(IllegalStateException.class, () -> pedido.setStatus(StatusPedido.ABERTO));
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    // ---------- Issue #18: itens em pedido fechado ----------

    @Test
    void naoDeveAdicionarItemEmPedidoConfirmado() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CONFIRMADO);
        ItemPedido novoItem = new ItemPedido(20L, "Refrigerante", 2, new BigDecimal("8.90"));

        assertThrows(IllegalStateException.class, () -> pedido.adicionarItem(novoItem));

        assertEquals(1, pedido.getItens().size());
        assertEquals(0, new BigDecimal("12.50").compareTo(pedido.getTotal()));
    }

    @Test
    void naoDeveAdicionarItemEmPedidoCancelado() {
        Pedido pedido = pedidoComUmItemNoStatus(StatusPedido.CANCELADO);
        ItemPedido novoItem = new ItemPedido(20L, "Refrigerante", 2, new BigDecimal("8.90"));

        assertThrows(IllegalStateException.class, () -> pedido.adicionarItem(novoItem));

        assertEquals(1, pedido.getItens().size());
        assertEquals(0, new BigDecimal("12.50").compareTo(pedido.getTotal()));
    }

    // ---------- equals / hashCode / toString ----------

    @Test
    void pedidosComMesmoIdDevemSerIguaisETerMesmoHashCode() {
        Pedido primeiro = new Pedido(1L);
        primeiro.setId(7L);
        Pedido segundo = new Pedido(2L);
        segundo.setId(7L);

        assertEquals(primeiro, segundo);
        assertEquals(primeiro.hashCode(), segundo.hashCode());
    }

    @Test
    void pedidosComIdsDiferentesNaoDevemSerIguais() {
        Pedido primeiro = new Pedido(1L);
        primeiro.setId(7L);
        Pedido segundo = new Pedido(1L);
        segundo.setId(8L);

        assertNotEquals(primeiro, segundo);
    }

    @Test
    void pedidosSemIdSoDevemSerIguaisASiMesmos() {
        Pedido primeiro = new Pedido(1L);
        Pedido segundo = new Pedido(1L);

        assertEquals(true, primeiro.equals(primeiro));
        assertNotEquals(primeiro, segundo);
    }

    @Test
    void pedidoNaoDeveSerIgualANuloNemAOutroTipo() {
        Pedido pedido = new Pedido(1L);
        pedido.setId(7L);

        assertFalse(pedido.equals(null));
        assertFalse(pedido.equals("7"));
    }

    @Test
    void toStringDeveConterOsDadosDoPedido() {
        Pedido pedido = new Pedido(1L);
        pedido.setId(7L);

        String texto = pedido.toString();

        assertTrue(texto.contains("id=7"));
        assertTrue(texto.contains("clienteId=1"));
        assertTrue(texto.contains("status=ABERTO"));
    }
}
