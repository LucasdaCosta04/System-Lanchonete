package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.model.ItemPedido;
import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.model.StatusPedido;
import br.unisul.lanchonete.repository.ClienteRepository;
import br.unisul.lanchonete.repository.PedidoRepository;
import br.unisul.lanchonete.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final Long PEDIDO_ID = 10L;
    private static final Long CLIENTE_ID = 1L;

    private static final Long LANCHE_ID = 1L;
    private static final String LANCHE_NOME = "X-Burguer";
    private static final BigDecimal LANCHE_PRECO = new BigDecimal("18.00");

    private static final Long BEBIDA_ID = 2L;
    private static final String BEBIDA_NOME = "Refrigerante";
    private static final BigDecimal BEBIDA_PRECO = new BigDecimal("6.00");

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private EstoqueService estoqueService;

    @InjectMocks
    private PedidoService service;

    // Pedido salvo (com id) contendo 2 X-Burguer e 3 Refrigerantes.
    private Pedido criarPedidoComItens() {
        Pedido pedido = new Pedido(CLIENTE_ID);
        pedido.setId(PEDIDO_ID);
        pedido.adicionarItem(new ItemPedido(LANCHE_ID, LANCHE_NOME, 2, LANCHE_PRECO));
        pedido.adicionarItem(new ItemPedido(BEBIDA_ID, BEBIDA_NOME, 3, BEBIDA_PRECO));
        return pedido;
    }

    private Pedido criarPedidoVazio() {
        Pedido pedido = new Pedido(CLIENTE_ID);
        pedido.setId(PEDIDO_ID);
        return pedido;
    }

    private void prepararBusca(Pedido pedido) {
        when(pedidoRepository.buscarPorId(PEDIDO_ID)).thenReturn(Optional.of(pedido));
    }

    // Faz o mock do repositório devolver o próprio pedido recebido em salvar.
    private void prepararSalvar() {
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    // ---------- Construtor ----------

    @Test
    void deveRecusarConstrucaoComDependenciasNulas() {
        EstoqueService estoque = mock(EstoqueService.class);

        assertThrows(NullPointerException.class,
                () -> new PedidoService(null, clienteRepository, produtoRepository, estoque));
        assertThrows(NullPointerException.class,
                () -> new PedidoService(pedidoRepository, null, produtoRepository, estoque));
        assertThrows(NullPointerException.class,
                () -> new PedidoService(pedidoRepository, clienteRepository, null, estoque));
        assertThrows(NullPointerException.class,
                () -> new PedidoService(pedidoRepository, clienteRepository, produtoRepository, null));
    }

    // ---------- Criação ----------

    @Test
    void deveCriarPedidoAbertoParaClienteExistente() {
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(new Cliente()));
        prepararSalvar();

        Pedido pedido = service.criarPedido(CLIENTE_ID);

        assertEquals(CLIENTE_ID, pedido.getClienteId());
        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
        assertTrue(pedido.getItens().isEmpty());
        verify(pedidoRepository).salvar(pedido);
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveRecusarCriacaoDePedidoComClienteNulo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(null));

        assertEquals("O id do cliente é obrigatório.", erro.getMessage());
        verify(pedidoRepository, never()).salvar(any());
    }

    @Test
    void deveRecusarCriacaoDePedidoComClienteInexistente() {
        when(clienteRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        ClienteNaoEncontradoException erro = assertThrows(ClienteNaoEncontradoException.class,
                () -> service.criarPedido(99L));

        assertTrue(erro.getMessage().contains("99"));
        verify(pedidoRepository, never()).salvar(any());
    }

    // ---------- Busca e listagem ----------

    @Test
    void deveBuscarPedidoPorIdExistente() {
        Pedido pedido = criarPedidoComItens();
        prepararBusca(pedido);

        Pedido encontrado = service.buscarPorId(PEDIDO_ID);

        assertSame(pedido, encontrado);
    }

    @Test
    void deveLancarErroAoBuscarPedidoInexistente() {
        when(pedidoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Pedido não encontrado: id 99", erro.getMessage());
    }

    @Test
    void deveListarTodosOsPedidos() {
        List<Pedido> pedidos = List.of(criarPedidoComItens(), new Pedido(CLIENTE_ID));
        when(pedidoRepository.listarTodos()).thenReturn(pedidos);

        List<Pedido> resultado = service.listarTodos();

        assertEquals(2, resultado.size());
        assertSame(pedidos, resultado);
    }

    // ---------- Adição de itens ----------

    @Test
    void deveAdicionarItemCopiandoNomeEPrecoAtualDoProduto() {
        Pedido pedido = criarPedidoVazio();
        prepararBusca(pedido);
        prepararSalvar();
        Produto produto = new Produto(LANCHE_NOME, "Lanche", LANCHE_PRECO, 50);
        produto.setId(LANCHE_ID);
        when(produtoRepository.buscarPorId(LANCHE_ID)).thenReturn(Optional.of(produto));

        Pedido atualizado = service.adicionarItem(PEDIDO_ID, LANCHE_ID, 2);

        assertEquals(1, atualizado.getItens().size());
        ItemPedido item = atualizado.getItens().get(0);
        assertEquals(LANCHE_ID, item.getProdutoId());
        assertEquals(LANCHE_NOME, item.getNomeProduto());
        assertEquals(2, item.getQuantidade());
        assertEquals(LANCHE_PRECO, item.getPrecoUnitario());
        assertEquals(new BigDecimal("36.00"), atualizado.getTotal());
        verify(pedidoRepository).salvar(pedido);
        verifyNoInteractions(estoqueService);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void deveRecusarItemComQuantidadeZeroOuNegativa(int quantidade) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.adicionarItem(PEDIDO_ID, LANCHE_ID, quantidade));

        assertEquals("A quantidade deve ser maior que zero.", erro.getMessage());
        verify(pedidoRepository, never()).salvar(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"CONFIRMADO", "CANCELADO"})
    void deveRecusarAdicionarItemAPedidoFechado(StatusPedido status) {
        Pedido pedido = criarPedidoComItens();
        pedido.setStatus(status);
        prepararBusca(pedido);

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> service.adicionarItem(PEDIDO_ID, LANCHE_ID, 1));

        assertTrue(erro.getMessage().contains(status.name()));
        assertEquals(2, pedido.getItens().size());
        verifyNoInteractions(produtoRepository);
        verify(pedidoRepository, never()).salvar(any());
    }

    @Test
    void deveRecusarAdicionarItemDeProdutoInexistente() {
        prepararBusca(criarPedidoVazio());
        when(produtoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class,
                () -> service.adicionarItem(PEDIDO_ID, 99L, 1));

        verify(pedidoRepository, never()).salvar(any());
    }

    // ---------- Confirmação ----------

    @Test
    void deveConfirmarPedidoEDarBaixaEmTodosOsItensQuandoHouverEstoque() {
        Pedido pedido = criarPedidoComItens();
        prepararBusca(pedido);
        prepararSalvar();
        when(estoqueService.possuiEstoque(LANCHE_ID, 2)).thenReturn(true);
        when(estoqueService.possuiEstoque(BEBIDA_ID, 3)).thenReturn(true);

        Pedido confirmado = service.confirmar(PEDIDO_ID);

        assertEquals(StatusPedido.CONFIRMADO, confirmado.getStatus());

        // Todos os itens são verificados antes da primeira baixa, e cada baixa usa a quantidade exata.
        InOrder ordem = inOrder(estoqueService);
        ordem.verify(estoqueService).possuiEstoque(LANCHE_ID, 2);
        ordem.verify(estoqueService).possuiEstoque(BEBIDA_ID, 3);
        ordem.verify(estoqueService).darBaixa(LANCHE_ID, 2);
        ordem.verify(estoqueService).darBaixa(BEBIDA_ID, 3);
        verify(pedidoRepository).salvar(pedido);
    }

    @Test
    void deveRecusarPedidoSemBaixaParcialQuandoEstoqueInsuficiente() {
        prepararBusca(criarPedidoComItens());
        when(estoqueService.possuiEstoque(LANCHE_ID, 2)).thenReturn(true);
        when(estoqueService.possuiEstoque(BEBIDA_ID, 3)).thenReturn(false);

        assertThrows(EstoqueInsuficienteException.class, () -> service.confirmar(PEDIDO_ID));

        // O X-Burguer tinha estoque, mas nem ele pode sofrer baixa.
        verify(estoqueService, never()).darBaixa(anyLong(), anyInt());
        verify(pedidoRepository, never()).salvar(any());
    }

    @Test
    void deveManterPedidoAbertoQuandoConfirmacaoForRecusada() {
        Pedido pedido = criarPedidoComItens();
        prepararBusca(pedido);
        when(estoqueService.possuiEstoque(LANCHE_ID, 2)).thenReturn(false);

        assertThrows(EstoqueInsuficienteException.class, () -> service.confirmar(PEDIDO_ID));

        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
    }

    @Test
    void deveInformarProdutoSemEstoqueNaMensagemDeErro() {
        prepararBusca(criarPedidoComItens());
        when(estoqueService.possuiEstoque(LANCHE_ID, 2)).thenReturn(true);
        when(estoqueService.possuiEstoque(BEBIDA_ID, 3)).thenReturn(false);

        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
                () -> service.confirmar(PEDIDO_ID));

        assertEquals("Pedido não confirmado: estoque insuficiente para o produto 'Refrigerante'.",
                erro.getMessage());
    }

    @Test
    void deveSomarQuantidadesDoMesmoProdutoAoVerificarEstoque() {
        Pedido pedido = criarPedidoVazio();
        pedido.adicionarItem(new ItemPedido(LANCHE_ID, LANCHE_NOME, 2, LANCHE_PRECO));
        pedido.adicionarItem(new ItemPedido(LANCHE_ID, LANCHE_NOME, 3, LANCHE_PRECO));
        prepararBusca(pedido);
        when(estoqueService.possuiEstoque(LANCHE_ID, 5)).thenReturn(false);

        assertThrows(EstoqueInsuficienteException.class, () -> service.confirmar(PEDIDO_ID));

        verify(estoqueService, never()).darBaixa(anyLong(), anyInt());
        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
    }

    @Test
    void deveRecusarConfirmacaoDePedidoSemItens() {
        prepararBusca(criarPedidoVazio());

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> service.confirmar(PEDIDO_ID));

        assertEquals("Não é possível confirmar um pedido sem itens.", erro.getMessage());
        verifyNoInteractions(estoqueService);
        verify(pedidoRepository, never()).salvar(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"CONFIRMADO", "CANCELADO"})
    void deveRecusarConfirmacaoDePedidoFechado(StatusPedido status) {
        Pedido pedido = criarPedidoComItens();
        pedido.setStatus(status);
        prepararBusca(pedido);

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> service.confirmar(PEDIDO_ID));

        assertTrue(erro.getMessage().contains(status.name()));
        assertEquals(status, pedido.getStatus());
        verifyNoInteractions(estoqueService);
        verify(pedidoRepository, never()).salvar(any());
    }

    // ---------- Cancelamento ----------

    @Test
    void naoDeveAlterarEstoqueAoCancelarPedidoAberto() {
        Pedido pedido = criarPedidoComItens();
        prepararBusca(pedido);
        prepararSalvar();

        Pedido cancelado = service.cancelar(PEDIDO_ID);

        assertEquals(StatusPedido.CANCELADO, cancelado.getStatus());
        verifyNoInteractions(estoqueService);
        verify(pedidoRepository).salvar(pedido);
    }

    @Test
    void deveDevolverItensAoEstoqueAoCancelarPedidoConfirmado() {
        Pedido pedido = criarPedidoComItens();
        pedido.setStatus(StatusPedido.CONFIRMADO);
        prepararBusca(pedido);
        prepararSalvar();

        Pedido cancelado = service.cancelar(PEDIDO_ID);

        assertEquals(StatusPedido.CANCELADO, cancelado.getStatus());
        verify(estoqueService).repor(LANCHE_ID, 2);
        verify(estoqueService).repor(BEBIDA_ID, 3);
        verify(estoqueService, never()).darBaixa(anyLong(), anyInt());
        verify(pedidoRepository).salvar(pedido);
    }

    @Test
    void deveLancarErroAoCancelarPedidoJaCancelado() {
        Pedido pedido = criarPedidoComItens();
        pedido.setStatus(StatusPedido.CANCELADO);
        prepararBusca(pedido);

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> service.cancelar(PEDIDO_ID));

        assertEquals("O pedido 10 já está cancelado.", erro.getMessage());
        verifyNoInteractions(estoqueService);
        verify(pedidoRepository, never()).salvar(any());
    }
}
