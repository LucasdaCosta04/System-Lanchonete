package br.unisul.lanchonete.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ProdutoTest {

    private static final String NOME = "X-Burguer";
    private static final String CATEGORIA = "Lanche";
    private static final BigDecimal PRECO = new BigDecimal("18.00");
    private static final int ESTOQUE = 10;

    @Test
    void deveCriarProdutoComOsDadosInformadosNoConstrutor() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        assertEquals(NOME, produto.getNome());
        assertEquals(CATEGORIA, produto.getCategoria());
        assertEquals(PRECO, produto.getPreco());
        assertEquals(ESTOQUE, produto.getQuantidadeEstoque());
    }

    @Test
    void deveAlterarNomeAtravesDoSetter() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        produto.setNome("Refrigerante");

        assertEquals("Refrigerante", produto.getNome());
    }

    @Test
    void deveAlterarCategoriaAtravesDoSetter() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        produto.setCategoria("Bebida");

        assertEquals("Bebida", produto.getCategoria());
    }

    @Test
    void deveAlterarPrecoAtravesDoSetter() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        produto.setPreco(new BigDecimal("6.00"));

        assertEquals(new BigDecimal("6.00"), produto.getPreco());
    }

    @Test
    void deveAlterarQuantidadeEstoqueAtravesDoSetter() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        produto.setQuantidadeEstoque(20);

        assertEquals(20, produto.getQuantidadeEstoque());
    }

    @Test
    void deveAlterarIdAtravesDoSetter() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        produto.setId(1L);

        assertEquals(1L, produto.getId());
    }

    @Test
    void produtoDeveSerIgualAEleMesmo() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto.setId(1L);

        assertTrue(produto.equals(produto));
    }

    @Test
    void produtosComMesmoIdDevemSerIguaisMesmoComNomesDiferentes() {
        Produto produto1 = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto1.setId(1L);
        Produto produto2 = new Produto("Refrigerante", "Bebida", new BigDecimal("6.00"), 20);
        produto2.setId(1L);

        assertEquals(produto1, produto2);
    }

    @Test
    void produtosComIdsDiferentesNaoDevemSerIguais() {
        Produto produto1 = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto1.setId(1L);
        Produto produto2 = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto2.setId(2L);

        assertNotEquals(produto1, produto2);
    }

    @Test
    void produtoSemIdSoDeveSerIgualAEleMesmo() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        assertTrue(produto.equals(produto));
    }

    @Test
    void produtoSemIdDeveSerDiferenteDeOutroProdutoSemId() {
        Produto produto1 = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        Produto produto2 = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);

        assertNotEquals(produto1, produto2);
    }

    @Test
    void produtoDeveSerDiferenteDeNulo() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto.setId(1L);

        assertFalse(produto.equals(null));
    }

    @Test
    void produtoDeveSerDiferenteDeObjetoDeOutraClasse() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto.setId(1L);

        assertFalse(produto.equals("não é um produto"));
    }

    @Test
    void produtosComMesmoIdDevemTerMesmoHashCode() {
        Produto produto1 = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto1.setId(1L);
        Produto produto2 = new Produto("Refrigerante", "Bebida", new BigDecimal("6.00"), 20);
        produto2.setId(1L);

        assertEquals(produto1.hashCode(), produto2.hashCode());
    }

    @Test
    void toStringDeveConterTodosOsCamposDoProduto() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        produto.setId(1L);

        String resultado = produto.toString();

        assertTrue(resultado.contains("id=1"));
        assertTrue(resultado.contains("nome='" + NOME + "'"));
        assertTrue(resultado.contains("categoria='" + CATEGORIA + "'"));
        assertTrue(resultado.contains("preco=" + PRECO));
        assertTrue(resultado.contains("quantidadeEstoque=" + ESTOQUE));
    }
}
