package br.unisul.lanchonete.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.model.ItemPedido;
import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.Produto;

class JsonTest {

    @Test
    void textoNuloDeveVirarNull() {
        assertEquals("null", Json.texto(null));
    }

    @Test
    void textoSimplesDeveFicarEntreAspasMantendoAcentos() {
        assertEquals("\"Porção de Batata\"", Json.texto("Porção de Batata"));
    }

    @Test
    void textoDeveEscaparCaracteresEspeciais() {
        String original = "a\"b\\c\n\r\t\u0001<x>";

        assertEquals("\"a\\\"b\\\\c\\n\\r\\t\\u0001\\u003cx\\u003e\"", Json.texto(original));
    }

    @Test
    void listaDeveJuntarObjetosEntreColchetes() {
        assertEquals("[]", Json.lista(List.of()));
        assertEquals("[1,2]", Json.lista(List.of("1", "2")));
    }

    @Test
    void erroDeveTerCampoErro() {
        assertEquals("{\"erro\":\"Falhou\"}", Json.erro("Falhou"));
    }

    @Test
    void clienteDeveTerIdNomeETelefoneSemCpf() {
        Cliente cliente = new Cliente("Maria Silva", "12345678909", null);
        cliente.setId(1L);

        assertEquals("{\"id\":1,\"nome\":\"Maria Silva\",\"telefone\":null}", Json.cliente(cliente));
    }

    @Test
    void produtoDeveTerPrecoComoNumeroEEstoque() {
        Produto produto = new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10);
        produto.setId(1L);

        assertEquals("{\"id\":1,\"nome\":\"X-Burguer\",\"categoria\":\"Lanche\",\"preco\":18.00,\"estoque\":10}",
                Json.produto(produto));
    }

    @Test
    void pedidoDeveTerClienteStatusTotalEItensComSubtotal() {
        Pedido pedido = new Pedido(1L);
        pedido.setId(10L);
        pedido.adicionarItem(new ItemPedido(1L, "X-Burguer", 2, new BigDecimal("18.00")));

        assertEquals("{\"id\":10,\"cliente\":\"Maria Silva\",\"status\":\"ABERTO\",\"total\":36.00,"
                + "\"itens\":[{\"produtoId\":1,\"nomeProduto\":\"X-Burguer\",\"quantidade\":2,"
                + "\"precoUnitario\":18.00,\"subtotal\":36.00}]}",
                Json.pedido(pedido, "Maria Silva"));
    }

    @Test
    void pedidoSemItensDeveTerTotalZeroEListaVazia() {
        Pedido pedido = new Pedido(1L);
        pedido.setId(11L);

        assertEquals("{\"id\":11,\"cliente\":\"Maria Silva\",\"status\":\"ABERTO\",\"total\":0,\"itens\":[]}",
                Json.pedido(pedido, "Maria Silva"));
    }
}
