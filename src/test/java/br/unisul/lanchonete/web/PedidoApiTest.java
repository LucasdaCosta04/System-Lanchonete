package br.unisul.lanchonete.web;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.model.ItemPedido;
import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.model.StatusPedido;
import br.unisul.lanchonete.service.ClienteNaoEncontradoException;
import br.unisul.lanchonete.service.ClienteService;
import br.unisul.lanchonete.service.EstoqueInsuficienteException;
import br.unisul.lanchonete.service.PedidoService;
import br.unisul.lanchonete.service.ProdutoNaoEncontradoException;
import br.unisul.lanchonete.service.ProdutoService;
import br.unisul.lanchonete.web.PedidoApi.Resposta;

@ExtendWith(MockitoExtension.class)
class PedidoApiTest {

    private static final Long CLIENTE_ID = 1L;
    private static final Long PEDIDO_ID = 10L;
    private static final String GET = "GET";
    private static final String POST = "POST";

    @Mock
    private PedidoService pedidoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ProdutoService produtoService;

    private PedidoApi api;

    @BeforeEach
    void setUp() {
        api = new PedidoApi(pedidoService, clienteService, produtoService);
    }

    private static Cliente maria() {
        Cliente cliente = new Cliente("Maria Silva", "12345678909", "(48) 99999-0000");
        cliente.setId(CLIENTE_ID);
        return cliente;
    }

    private static Pedido pedido(StatusPedido status) {
        Pedido pedido = new Pedido(CLIENTE_ID);
        pedido.setId(PEDIDO_ID);
        pedido.adicionarItem(new ItemPedido(2L, "Refrigerante", 3, new BigDecimal("6.00")));
        if (status != StatusPedido.ABERTO) {
            pedido.setStatus(status);
        }
        return pedido;
    }

    private void clienteMariaExiste() {
        when(clienteService.buscarPorId(CLIENTE_ID)).thenReturn(maria());
    }

    private static void assertErro(Resposta resposta, int status, String mensagem) {
        assertAll(
                () -> assertEquals(status, resposta.status()),
                () -> assertEquals(Json.erro(mensagem), resposta.corpo()));
    }

    @Test
    void construtorDeveExigirTodosOsServicos() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new PedidoApi(null, clienteService, produtoService)),
                () -> assertThrows(NullPointerException.class, () -> new PedidoApi(pedidoService, null, produtoService)),
                () -> assertThrows(NullPointerException.class, () -> new PedidoApi(pedidoService, clienteService, null)));
    }

    // ---------- Listagens (GET) ----------

    @Test
    void getClientesDeveListarClientesEmJson() {
        when(clienteService.listarTodos()).thenReturn(List.of(maria()));

        Resposta resposta = api.processar(GET, "/api/clientes", "");

        assertEquals(200, resposta.status());
        assertEquals("[" + Json.cliente(maria()) + "]", resposta.corpo());
    }

    @Test
    void getProdutosDeveListarProdutosComEstoque() {
        Produto lanche = new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), 10);
        lanche.setId(1L);
        when(produtoService.listarTodos()).thenReturn(List.of(lanche));

        Resposta resposta = api.processar(GET, "/api/produtos", "");

        assertEquals(200, resposta.status());
        assertEquals("[" + Json.produto(lanche) + "]", resposta.corpo());
    }

    @Test
    void getPedidosDeveListarPedidosComNomeDoCliente() {
        Pedido pedido = pedido(StatusPedido.ABERTO);
        when(pedidoService.listarTodos()).thenReturn(List.of(pedido));
        clienteMariaExiste();

        Resposta resposta = api.processar(GET, "/api/pedidos", null);

        assertEquals(200, resposta.status());
        assertEquals("[" + Json.pedido(pedido, "Maria Silva") + "]", resposta.corpo());
    }

    @Test
    void getEmRotaDesconhecidaDeveRetornar404() {
        assertErro(api.processar(GET, "/api/desconhecida", ""), 404, "Rota não encontrada: /api/desconhecida");
    }

    // ---------- Ações (POST) ----------

    @Test
    void postPedidosDeveCriarPedidoERetornar201() {
        Pedido novo = new Pedido(CLIENTE_ID);
        novo.setId(PEDIDO_ID);
        when(pedidoService.criarPedido(CLIENTE_ID)).thenReturn(novo);
        clienteMariaExiste();

        Resposta resposta = api.processar(POST, "/api/pedidos", "clienteId=1");

        assertEquals(201, resposta.status());
        assertEquals(Json.pedido(novo, "Maria Silva"), resposta.corpo());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "clienteId=", "clienteId=%20%20", "outroCampo=1" })
    void postPedidosSemClienteIdDeveRetornar400(String corpo) {
        assertErro(api.processar(POST, "/api/pedidos", corpo), 400, "O campo 'clienteId' é obrigatório.");
        verifyNoInteractions(pedidoService);
    }

    @ParameterizedTest
    @ValueSource(strings = { "clienteId=abc", "clienteId=1.5", "clienteId=99999999999" })
    void postPedidosComClienteIdNaoInteiroDeveRetornar400(String corpo) {
        assertErro(api.processar(POST, "/api/pedidos", corpo), 400, "O campo 'clienteId' deve ser um número inteiro.");
        verifyNoInteractions(pedidoService);
    }

    @Test
    void postItensDeveAdicionarItemComProdutoEQuantidade() {
        Pedido atualizado = pedido(StatusPedido.ABERTO);
        when(pedidoService.adicionarItem(PEDIDO_ID, 2L, 3)).thenReturn(atualizado);
        clienteMariaExiste();

        Resposta resposta = api.processar(POST, "/api/pedidos/10/itens", "produtoId=2&quantidade=3");

        assertEquals(200, resposta.status());
        assertEquals(Json.pedido(atualizado, "Maria Silva"), resposta.corpo());
        verify(pedidoService).adicionarItem(PEDIDO_ID, 2L, 3);
    }

    @Test
    void postItensSemQuantidadeDeveRetornar400() {
        assertErro(api.processar(POST, "/api/pedidos/10/itens", "produtoId=2"), 400,
                "O campo 'quantidade' é obrigatório.");
        verifyNoInteractions(pedidoService);
    }

    @Test
    void postConfirmarDeveConfirmarPedido() {
        Pedido confirmado = pedido(StatusPedido.CONFIRMADO);
        when(pedidoService.confirmar(PEDIDO_ID)).thenReturn(confirmado);
        clienteMariaExiste();

        Resposta resposta = api.processar(POST, "/api/pedidos/10/confirmar", "");

        assertEquals(200, resposta.status());
        assertTrue(resposta.corpo().contains("\"status\":\"CONFIRMADO\""));
    }

    @Test
    void postCancelarDeveCancelarPedido() {
        Pedido cancelado = pedido(StatusPedido.CANCELADO);
        when(pedidoService.cancelar(PEDIDO_ID)).thenReturn(cancelado);
        clienteMariaExiste();

        Resposta resposta = api.processar(POST, "/api/pedidos/10/cancelar", "");

        assertEquals(200, resposta.status());
        assertTrue(resposta.corpo().contains("\"status\":\"CANCELADO\""));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/api/pedidos/10/pagar", "/api/pedidos/abc/confirmar", "/api/pedidos/10",
        "/api/pedidos/1234567890/confirmar", "/api/clientes" })
    void postEmRotaDesconhecidaDeveRetornar404(String caminho) {
        assertErro(api.processar(POST, caminho, ""), 404, "Rota não encontrada: " + caminho);
        verifyNoInteractions(pedidoService);
    }

    @ParameterizedTest
    @ValueSource(strings = { "DELETE", "PUT", "PATCH" })
    void metodoNaoSuportadoDeveRetornar405(String metodo) {
        assertErro(api.processar(metodo, "/api/pedidos", ""), 405, "Método não permitido: " + metodo);
    }

    // ---------- Tradução das exceções em códigos HTTP ----------

    @Test
    void clienteNaoEncontradoDeveRetornar404() {
        when(pedidoService.criarPedido(99L)).thenThrow(new ClienteNaoEncontradoException("Cliente não encontrado: id 99"));

        assertErro(api.processar(POST, "/api/pedidos", "clienteId=99"), 404, "Cliente não encontrado: id 99");
    }

    @Test
    void produtoNaoEncontradoDeveRetornar404() {
        when(pedidoService.adicionarItem(PEDIDO_ID, 99L, 1))
                .thenThrow(new ProdutoNaoEncontradoException("Produto não encontrado com id: 99"));

        assertErro(api.processar(POST, "/api/pedidos/10/itens", "produtoId=99&quantidade=1"), 404,
                "Produto não encontrado com id: 99");
    }

    @Test
    void pedidoInexistenteDeveRetornar400() {
        when(pedidoService.confirmar(99L)).thenThrow(new IllegalArgumentException("Pedido não encontrado: id 99"));

        assertErro(api.processar(POST, "/api/pedidos/99/confirmar", ""), 400, "Pedido não encontrado: id 99");
    }

    @Test
    void quantidadeInvalidaDeveRetornar400() {
        when(pedidoService.adicionarItem(PEDIDO_ID, 2L, 0))
                .thenThrow(new IllegalArgumentException("A quantidade deve ser maior que zero."));

        assertErro(api.processar(POST, "/api/pedidos/10/itens", "produtoId=2&quantidade=0"), 400,
                "A quantidade deve ser maior que zero.");
    }

    @Test
    void estoqueInsuficienteDeveRetornar409() {
        String mensagem = "Pedido não confirmado: estoque insuficiente para o produto 'Batata Frita'.";
        when(pedidoService.confirmar(PEDIDO_ID)).thenThrow(new EstoqueInsuficienteException(mensagem));

        assertErro(api.processar(POST, "/api/pedidos/10/confirmar", ""), 409, mensagem);
    }

    @Test
    void conflitoDeStatusDeveRetornar409() {
        when(pedidoService.cancelar(PEDIDO_ID)).thenThrow(new IllegalStateException("O pedido 10 já está cancelado."));

        assertErro(api.processar(POST, "/api/pedidos/10/cancelar", ""), 409, "O pedido 10 já está cancelado.");
    }

    @Test
    void corpoComCodificacaoInvalidaDeveRetornar400() {
        assertErro(api.processar(POST, "/api/pedidos", "clienteId=%zz"), 400,
                "Os dados enviados estão em um formato inválido.");
        verifyNoInteractions(pedidoService);
    }

    // ---------- Leitura do formulário ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void lerFormularioVazioDeveRetornarMapaVazio(String corpo) {
        assertTrue(PedidoApi.lerFormulario(corpo).isEmpty());
    }

    @Test
    void lerFormularioDeveDecodificarCamposEAceitarCampoSemValor() {
        Map<String, String> campos = PedidoApi.lerFormulario("nome=Batata+Frita&categoria=Por%C3%A7%C3%A3o&vazio");

        assertEquals(Map.of("nome", "Batata Frita", "categoria", "Porção", "vazio", ""), campos);
    }

    @Test
    void lerFormularioComCodificacaoInvalidaDeveLancarExcecaoEmPortugues() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> PedidoApi.lerFormulario("a=%zz"));

        assertEquals("Os dados enviados estão em um formato inválido.", erro.getMessage());
    }
}
