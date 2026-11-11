package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.model.StatusPedido;
import br.unisul.lanchonete.repository.ClienteRepositoryEmMemoria;
import br.unisul.lanchonete.repository.PedidoRepositoryEmMemoria;
import br.unisul.lanchonete.repository.ProdutoRepositoryEmMemoria;

/**
 * Testa o fluxo de pedidos de ponta a ponta com os serviços e repositórios em memória reais, sem mocks.
 * O CPF usado é fictício, calculado a partir da base 123456789 com a regra dos dígitos verificadores.
 */
@Tag("integracao")
@DisplayName("Integração: PedidoService + EstoqueService + repositórios em memória")
class PedidoServiceIntegracaoTest {

    private static final String CPF_VALIDO = "12345678909";

    private static final int ESTOQUE_LANCHE = 10;
    private static final int ESTOQUE_BEBIDA = 2;
    private static final int ESTOQUE_BATATA = 5;

    private ProdutoRepositoryEmMemoria produtoRepository;
    private PedidoService pedidoService;

    private Long clienteId;
    private Long lancheId;
    private Long bebidaId;
    private Long batataId;

    @BeforeEach
    void montarSistemaComDadosIniciais() {
        ClienteRepositoryEmMemoria clienteRepository = new ClienteRepositoryEmMemoria();
        produtoRepository = new ProdutoRepositoryEmMemoria();
        PedidoRepositoryEmMemoria pedidoRepository = new PedidoRepositoryEmMemoria();

        ClienteService clienteService = new ClienteService(clienteRepository);
        ProdutoService produtoService = new ProdutoService(produtoRepository);
        EstoqueService estoqueService = new EstoqueService(produtoRepository);
        pedidoService = new PedidoService(pedidoRepository, clienteRepository, produtoRepository, estoqueService);

        clienteId = clienteService.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, "(48) 99999-0000")).getId();
        lancheId = produtoService.cadastrar(
                new Produto("X-Burguer", "Lanche", new BigDecimal("18.00"), ESTOQUE_LANCHE)).getId();
        bebidaId = produtoService.cadastrar(
                new Produto("Refrigerante", "Bebida", new BigDecimal("6.00"), ESTOQUE_BEBIDA)).getId();
        batataId = produtoService.cadastrar(
                new Produto("Batata Frita", "Porção", new BigDecimal("9.50"), ESTOQUE_BATATA)).getId();
    }

    // Lê o saldo direto do repositório, para comprovar o que de fato ficou gravado.
    private int saldo(Long produtoId) {
        return produtoRepository.buscarPorId(produtoId).orElseThrow().getQuantidadeEstoque();
    }

    // Cria um pedido com 2 X-Burguer, 2 Refrigerantes e 1 Batata Frita.
    private Pedido criarPedidoComTresItens() {
        Long pedidoId = pedidoService.criarPedido(clienteId).getId();
        pedidoService.adicionarItem(pedidoId, lancheId, 2);
        pedidoService.adicionarItem(pedidoId, bebidaId, 2);
        return pedidoService.adicionarItem(pedidoId, batataId, 1);
    }

    // ---------- 1. Fluxo completo ----------

    @Test
    void deveConfirmarPedidoCompletoDandoBaixaNoEstoqueECalculandoOTotal() {
        Pedido pedido = criarPedidoComTresItens();

        Pedido confirmado = pedidoService.confirmar(pedido.getId());

        assertAll(
                () -> assertEquals(StatusPedido.CONFIRMADO, pedidoService.buscarPorId(pedido.getId()).getStatus()),
                () -> assertEquals(3, confirmado.getItens().size()),
                // 2 x 18,00 + 2 x 6,00 + 1 x 9,50
                () -> assertEquals(new BigDecimal("57.50"), confirmado.getTotal()),
                () -> assertEquals(8, saldo(lancheId)),
                () -> assertEquals(0, saldo(bebidaId)),
                () -> assertEquals(4, saldo(batataId)));
    }

    // ---------- 2. Recusa sem baixa parcial ----------

    @Test
    void deveRecusarConfirmacaoSemBaixaParcialQuandoSegundoItemNaoTemEstoque() {
        Long pedidoId = pedidoService.criarPedido(clienteId).getId();
        pedidoService.adicionarItem(pedidoId, lancheId, 3);
        pedidoService.adicionarItem(pedidoId, bebidaId, ESTOQUE_BEBIDA + 1);

        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
                () -> pedidoService.confirmar(pedidoId));

        assertAll(
                () -> assertTrue(erro.getMessage().contains("Refrigerante")),
                () -> assertEquals(StatusPedido.ABERTO, pedidoService.buscarPorId(pedidoId).getStatus()),
                // O X-Burguer tinha estoque, mas não pode ter sofrido baixa.
                () -> assertEquals(ESTOQUE_LANCHE, saldo(lancheId)),
                () -> assertEquals(ESTOQUE_BEBIDA, saldo(bebidaId)));
    }

    // ---------- 3. Cancelamento de pedido CONFIRMADO ----------

    @Test
    void deveDevolverTodosOsSaldosAoCancelarPedidoConfirmado() {
        Pedido pedido = criarPedidoComTresItens();
        pedidoService.confirmar(pedido.getId());

        Pedido cancelado = pedidoService.cancelar(pedido.getId());

        assertAll(
                () -> assertEquals(StatusPedido.CANCELADO, cancelado.getStatus()),
                () -> assertEquals(ESTOQUE_LANCHE, saldo(lancheId)),
                () -> assertEquals(ESTOQUE_BEBIDA, saldo(bebidaId)),
                () -> assertEquals(ESTOQUE_BATATA, saldo(batataId)));
    }

    // ---------- 4. Cancelamento de pedido ABERTO ----------

    @Test
    void naoDeveAlterarEstoqueAoCancelarPedidoAberto() {
        Pedido pedido = criarPedidoComTresItens();

        Pedido cancelado = pedidoService.cancelar(pedido.getId());

        assertAll(
                () -> assertEquals(StatusPedido.CANCELADO, cancelado.getStatus()),
                () -> assertEquals(ESTOQUE_LANCHE, saldo(lancheId)),
                () -> assertEquals(ESTOQUE_BEBIDA, saldo(bebidaId)),
                () -> assertEquals(ESTOQUE_BATATA, saldo(batataId)));
    }

    // ---------- 5. Regressões da Issue #18 ----------

    @Test
    void deveBloquearSegundoCancelamentoSemDevolverEstoqueEmDobro() {
        Pedido pedido = criarPedidoComTresItens();
        pedidoService.confirmar(pedido.getId());
        pedidoService.cancelar(pedido.getId());

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> pedidoService.cancelar(pedido.getId()));

        assertAll(
                () -> assertTrue(erro.getMessage().contains("já está cancelado")),
                () -> assertEquals(StatusPedido.CANCELADO, pedidoService.buscarPorId(pedido.getId()).getStatus()),
                // Se o segundo cancelamento passasse, o estoque seria reposto duas vezes.
                () -> assertEquals(ESTOQUE_LANCHE, saldo(lancheId)),
                () -> assertEquals(ESTOQUE_BEBIDA, saldo(bebidaId)),
                () -> assertEquals(ESTOQUE_BATATA, saldo(batataId)));
    }

    @Test
    void deveBloquearConfirmacaoDePedidoCancelado() {
        Pedido pedido = criarPedidoComTresItens();
        pedidoService.cancelar(pedido.getId());

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> pedidoService.confirmar(pedido.getId()));

        assertAll(
                () -> assertTrue(erro.getMessage().contains("CANCELADO")),
                () -> assertEquals(StatusPedido.CANCELADO, pedidoService.buscarPorId(pedido.getId()).getStatus()),
                () -> assertEquals(ESTOQUE_LANCHE, saldo(lancheId)),
                () -> assertEquals(ESTOQUE_BEBIDA, saldo(bebidaId)),
                () -> assertEquals(ESTOQUE_BATATA, saldo(batataId)));
    }

    @Test
    void deveBloquearInclusaoDeItemAposConfirmacao() {
        Pedido pedido = criarPedidoComTresItens();
        pedidoService.confirmar(pedido.getId());

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> pedidoService.adicionarItem(pedido.getId(), lancheId, 1));

        Pedido salvo = pedidoService.buscarPorId(pedido.getId());
        assertAll(
                () -> assertTrue(erro.getMessage().contains("CONFIRMADO")),
                () -> assertEquals(3, salvo.getItens().size()),
                () -> assertEquals(new BigDecimal("57.50"), salvo.getTotal()));
    }

    @Test
    void deveBloquearInclusaoDeItemAposCancelamento() {
        Pedido pedido = criarPedidoComTresItens();
        pedidoService.cancelar(pedido.getId());

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> pedidoService.adicionarItem(pedido.getId(), lancheId, 1));

        assertAll(
                () -> assertTrue(erro.getMessage().contains("CANCELADO")),
                () -> assertEquals(3, pedidoService.buscarPorId(pedido.getId()).getItens().size()));
    }
}
