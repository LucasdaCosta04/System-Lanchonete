package br.unisul.lanchonete.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unisul.lanchonete.model.Cliente;

/**
 * Os CPFs usados aqui são fictícios, calculados a partir das bases
 * sequenciais 123456789 e 987654321.
 */
class ClienteRepositoryEmMemoriaTest {

    private ClienteRepositoryEmMemoria repositorio;

    @BeforeEach
    void criarRepositorio() {
        repositorio = new ClienteRepositoryEmMemoria();
    }

    @Test
    void salvarClienteSemIdDeveGerarIdsSequenciais() {
        Cliente primeiro = repositorio.salvar(new Cliente("Ana Teste", "12345678909", "48999990001"));
        Cliente segundo = repositorio.salvar(new Cliente("Bruno Teste", "98765432100", "48999990002"));

        assertEquals(1L, primeiro.getId());
        assertEquals(2L, segundo.getId());
    }

    @Test
    void salvarClienteComIdExistenteDeveSubstituirOsDados() {
        Cliente original = repositorio.salvar(new Cliente("Ana Teste", "12345678909", "48999990001"));

        Cliente atualizado = new Cliente("Ana Atualizada", "12345678909", "48999990009");
        atualizado.setId(original.getId());
        repositorio.salvar(atualizado);

        Cliente encontrado = repositorio.buscarPorId(original.getId()).orElseThrow();
        assertEquals("Ana Atualizada", encontrado.getNome());
        assertEquals("48999990009", encontrado.getTelefone());
        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void buscarPorIdExistenteDeveRetornarOCliente() {
        Cliente salvo = repositorio.salvar(new Cliente("Ana Teste", "12345678909", "48999990001"));

        Optional<Cliente> encontrado = repositorio.buscarPorId(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Ana Teste", encontrado.get().getNome());
    }

    @Test
    void buscarPorIdInexistenteDeveRetornarOptionalVazio() {
        Optional<Cliente> encontrado = repositorio.buscarPorId(99L);

        assertTrue(encontrado.isEmpty());
    }

    @Test
    void buscarPorCpfComOuSemPontuacaoDeveEncontrarOMesmoCliente() {
        Cliente salvo = repositorio.salvar(new Cliente("Ana Teste", "123.456.789-09", "48999990001"));

        Optional<Cliente> semPontuacao = repositorio.buscarPorCpf("12345678909");
        Optional<Cliente> comPontuacao = repositorio.buscarPorCpf("123.456.789-09");

        assertEquals(salvo, semPontuacao.orElseThrow());
        assertEquals(salvo, comPontuacao.orElseThrow());
    }

    @Test
    void buscarPorCpfInexistenteDeveRetornarOptionalVazio() {
        repositorio.salvar(new Cliente("Ana Teste", "12345678909", "48999990001"));

        Optional<Cliente> encontrado = repositorio.buscarPorCpf("98765432100");

        assertTrue(encontrado.isEmpty());
    }

    @Test
    void listarTodosComRepositorioVazioDeveRetornarListaVazia() {
        assertTrue(repositorio.listarTodos().isEmpty());
    }

    @Test
    void listarTodosDeveRetornarClientesNaOrdemDeInsercao() {
        repositorio.salvar(new Cliente("Ana Teste", "12345678909", "48999990001"));
        repositorio.salvar(new Cliente("Bruno Teste", "98765432100", "48999990002"));

        List<Cliente> clientes = repositorio.listarTodos();

        assertEquals(2, clientes.size());
        assertEquals("Ana Teste", clientes.get(0).getNome());
        assertEquals("Bruno Teste", clientes.get(1).getNome());
    }

    @Test
    void alterarListaRetornadaNaoDeveAfetarORepositorio() {
        repositorio.salvar(new Cliente("Ana Teste", "12345678909", "48999990001"));

        List<Cliente> clientes = repositorio.listarTodos();
        clientes.clear();

        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void excluirDeveRemoverOCliente() {
        Cliente salvo = repositorio.salvar(new Cliente("Ana Teste", "12345678909", "48999990001"));

        repositorio.excluir(salvo.getId());

        assertTrue(repositorio.buscarPorId(salvo.getId()).isEmpty());
        assertTrue(repositorio.listarTodos().isEmpty());
    }

    @Test
    void excluirIdInexistenteNaoDeveLancarExcecao() {
        assertDoesNotThrow(() -> repositorio.excluir(99L));
    }
}
