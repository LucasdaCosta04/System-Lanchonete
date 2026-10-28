package br.unisul.lanchonete.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/**
 * Os valores BigDecimal são comparados com compareTo, para que a escala
 * (ex.: 37.5 e 37.50) não influencie o resultado.
 */
class ItemPedidoTest {

    @Test
    void subtotalComValoresInteirosDeveSerQuantidadeVezesPreco() {
        ItemPedido item = new ItemPedido(1L, "X-Burguer", 2, new BigDecimal("15"));

        assertEquals(0, new BigDecimal("30").compareTo(item.getSubtotal()));
    }

    @Test
    void subtotalComValoresDecimaisDeveSerQuantidadeVezesPreco() {
        ItemPedido item = new ItemPedido(1L, "X-Salada", 3, new BigDecimal("12.50"));

        assertEquals(0, new BigDecimal("37.50").compareTo(item.getSubtotal()));
    }

    @Test
    void subtotalComPrecoZeroDeveSerZero() {
        ItemPedido item = new ItemPedido(1L, "Sachê de ketchup", 4, BigDecimal.ZERO);

        assertEquals(0, BigDecimal.ZERO.compareTo(item.getSubtotal()));
    }

    @Test
    void gettersDevemRetornarOsValoresInformados() {
        BigDecimal preco = new BigDecimal("8.90");
        ItemPedido item = new ItemPedido(5L, "Refrigerante", 2, preco);

        assertEquals(5L, item.getProdutoId());
        assertEquals("Refrigerante", item.getNomeProduto());
        assertEquals(2, item.getQuantidade());
        assertEquals(preco, item.getPrecoUnitario());
    }

    @Test
    void toStringDeveConterOsDadosDoItem() {
        ItemPedido item = new ItemPedido(5L, "Refrigerante", 2, new BigDecimal("8.90"));

        String texto = item.toString();

        assertTrue(texto.contains("produtoId=5"));
        assertTrue(texto.contains("nomeProduto='Refrigerante'"));
        assertTrue(texto.contains("quantidade=2"));
        assertTrue(texto.contains("precoUnitario=8.90"));
    }

    @Test
    void criarItemSemProdutoIdDeveLancarExcecao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(null, "X-Burguer", 1, new BigDecimal("15.00")));

        assertEquals("O id do produto é obrigatório.", erro.getMessage());
    }

    @Test
    void criarItemSemNomeDeveLancarExcecao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(1L, null, 1, new BigDecimal("15.00")));

        assertEquals("O nome do produto é obrigatório.", erro.getMessage());
    }

    @Test
    void criarItemComNomeEmBrancoDeveLancarExcecao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(1L, "   ", 1, new BigDecimal("15.00")));

        assertEquals("O nome do produto é obrigatório.", erro.getMessage());
    }

    @Test
    void criarItemComQuantidadeZeroDeveLancarExcecao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(1L, "X-Burguer", 0, new BigDecimal("15.00")));

        assertEquals("A quantidade deve ser maior que zero.", erro.getMessage());
    }

    @Test
    void criarItemComQuantidadeNegativaDeveLancarExcecao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(1L, "X-Burguer", -1, new BigDecimal("15.00")));

        assertEquals("A quantidade deve ser maior que zero.", erro.getMessage());
    }

    @Test
    void criarItemSemPrecoDeveLancarExcecao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(1L, "X-Burguer", 1, null));

        assertEquals("O preço unitário é obrigatório.", erro.getMessage());
    }

    @Test
    void criarItemComPrecoNegativoDeveLancarExcecao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(1L, "X-Burguer", 1, new BigDecimal("-0.01")));

        assertEquals("O preço unitário não pode ser negativo.", erro.getMessage());
    }
}
