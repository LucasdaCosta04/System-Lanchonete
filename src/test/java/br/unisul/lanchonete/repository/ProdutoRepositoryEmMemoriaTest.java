package br.unisul.lanchonete.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unisul.lanchonete.model.Produto;

class ProdutoRepositoryEmMemoriaTest {

    private ProdutoRepositoryEmMemoria repositorio;

    @BeforeEach
    void criarRepositorio() {
        repositorio = new ProdutoRepositoryEmMemoria();
    }

    @Test
    void salvarProdutoSemIdDeveGerarIdsSequenciais() {
        Produto primeiro = repositorio.salvar(new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10));
        Produto segundo = repositorio.salvar(new Produto("Refrigerante", "Bebida", new BigDecimal("6.00"), 20));

        assertEquals(1L, primeiro.getId());
        assertEquals(2L, segundo.getId());
    }

    @Test
    void salvarProdutoComIdExistenteDeveSubstituirOAnterior() {
        Produto original = repositorio.salvar(new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10));

        Produto atualizado = new Produto("X-Burguer Duplo", "Lanche", new BigDecimal("22.00"), 5);
        atualizado.setId(original.getId());
        repositorio.salvar(atualizado);

        Produto encontrado = repositorio.buscarPorId(original.getId()).orElseThrow();
        assertEquals("X-Burguer Duplo", encontrado.getNome());
        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void buscarPorIdExistenteDeveRetornarOProduto() {
        Produto salvo = repositorio.salvar(new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10));

        Optional<Produto> encontrado = repositorio.buscarPorId(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("X-Burguer", encontrado.get().getNome());
    }

    @Test
    void buscarPorIdInexistenteDeveRetornarOptionalVazio() {
        Optional<Produto> encontrado = repositorio.buscarPorId(99L);

        assertTrue(encontrado.isEmpty());
    }

    @Test
    void listarTodosDeveRetornarProdutosNaOrdemDeInsercao() {
        repositorio.salvar(new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10));
        repositorio.salvar(new Produto("Refrigerante", "Bebida", new BigDecimal("6.00"), 20));

        List<Produto> produtos = repositorio.listarTodos();

        assertEquals(2, produtos.size());
        assertEquals("X-Burguer", produtos.get(0).getNome());
        assertEquals("Refrigerante", produtos.get(1).getNome());
    }

    @Test
    void alterarListaRetornadaNaoDeveAfetarORepositorio() {
        repositorio.salvar(new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10));

        List<Produto> produtos = repositorio.listarTodos();
        produtos.clear();

        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void excluirDeveRemoverOProduto() {
        Produto salvo = repositorio.salvar(new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10));

        repositorio.excluir(salvo.getId());

        assertTrue(repositorio.buscarPorId(salvo.getId()).isEmpty());
    }
}
