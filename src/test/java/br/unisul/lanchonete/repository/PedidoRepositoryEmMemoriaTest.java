package br.unisul.lanchonete.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.StatusPedido;

class PedidoRepositoryEmMemoriaTest {

    private PedidoRepositoryEmMemoria repositorio;

    @BeforeEach
    void criarRepositorio() {
        repositorio = new PedidoRepositoryEmMemoria();
    }

    @Test
    void salvarPedidoSemIdDeveGerarIdsSequenciais() {
        Pedido primeiro = repositorio.salvar(new Pedido(1L));
        Pedido segundo = repositorio.salvar(new Pedido(2L));

        assertEquals(1L, primeiro.getId());
        assertEquals(2L, segundo.getId());
    }

    @Test
    void salvarPedidoComIdExistenteDeveSubstituirOsDados() {
        Pedido original = repositorio.salvar(new Pedido(1L));

        Pedido atualizado = new Pedido(1L);
        atualizado.setId(original.getId());
        atualizado.setStatus(StatusPedido.CONFIRMADO);
        repositorio.salvar(atualizado);

        Pedido encontrado = repositorio.buscarPorId(original.getId()).orElseThrow();
        assertSame(atualizado, encontrado);
        assertEquals(StatusPedido.CONFIRMADO, encontrado.getStatus());
        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void buscarPorIdExistenteDeveRetornarOPedido() {
        Pedido salvo = repositorio.salvar(new Pedido(1L));

        Optional<Pedido> encontrado = repositorio.buscarPorId(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(1L, encontrado.get().getClienteId());
    }

    @Test
    void buscarPorIdInexistenteDeveRetornarOptionalVazio() {
        Optional<Pedido> encontrado = repositorio.buscarPorId(99L);

        assertTrue(encontrado.isEmpty());
    }

    @Test
    void listarTodosComRepositorioVazioDeveRetornarListaVazia() {
        assertTrue(repositorio.listarTodos().isEmpty());
    }

    @Test
    void listarTodosDeveRetornarPedidosNaOrdemDeInsercao() {
        repositorio.salvar(new Pedido(10L));
        repositorio.salvar(new Pedido(20L));

        List<Pedido> pedidos = repositorio.listarTodos();

        assertEquals(2, pedidos.size());
        assertEquals(10L, pedidos.get(0).getClienteId());
        assertEquals(20L, pedidos.get(1).getClienteId());
    }

    @Test
    void alterarListaRetornadaNaoDeveAfetarORepositorio() {
        repositorio.salvar(new Pedido(1L));

        List<Pedido> pedidos = repositorio.listarTodos();
        pedidos.clear();

        assertEquals(1, repositorio.listarTodos().size());
    }
}
