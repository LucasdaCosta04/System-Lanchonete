package br.unisul.lanchonete.service;

import java.util.List;
import java.util.Objects;

import br.unisul.lanchonete.model.ItemPedido;
import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.model.StatusPedido;
import br.unisul.lanchonete.repository.ClienteRepository;
import br.unisul.lanchonete.repository.PedidoRepository;
import br.unisul.lanchonete.repository.ProdutoRepository;

/**
 * Contém as regras de negócio dos pedidos: criação, itens, confirmação e cancelamento.
 */
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueService estoqueService;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository, EstoqueService estoqueService) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "O repositório de pedidos é obrigatório.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "O repositório de clientes é obrigatório.");
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "O repositório de produtos é obrigatório.");
        this.estoqueService = Objects.requireNonNull(estoqueService, "O serviço de estoque é obrigatório.");
    }

    /**
     * Cria um novo pedido ABERTO para um cliente existente. Não mexe no estoque.
     *
     * @param clienteId id do cliente
     * @return o pedido salvo, com o id preenchido
     * @throws IllegalArgumentException      se o id do cliente for nulo
     * @throws ClienteNaoEncontradoException se não existir cliente com o id
     */
    public Pedido criarPedido(Long clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("O id do cliente é obrigatório.");
        }
        if (clienteRepository.buscarPorId(clienteId).isEmpty()) {
            throw new ClienteNaoEncontradoException("Cliente não encontrado: id " + clienteId);
        }

        return pedidoRepository.salvar(new Pedido(clienteId));
    }

    /**
     * Busca um pedido pelo id.
     *
     * @param pedidoId id do pedido
     * @return o pedido encontrado
     * @throws IllegalArgumentException se não existir pedido com o id
     */
    public Pedido buscarPorId(Long pedidoId) {
        return pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: id " + pedidoId));
    }

    /**
     * Lista todos os pedidos cadastrados.
     *
     * @return lista de pedidos, vazia se não houver nenhum
     */
    public List<Pedido> listarTodos() {
        return pedidoRepository.listarTodos();
    }

    /**
     * Adiciona um item a um pedido ABERTO, copiando o preço atual do produto.
     *
     * @param pedidoId   id do pedido
     * @param produtoId  id do produto
     * @param quantidade quantidade do produto
     * @return o pedido atualizado
     * @throws IllegalArgumentException      se a quantidade não for maior que zero ou o pedido não existir
     * @throws IllegalStateException         se o pedido não estiver ABERTO
     * @throws ProdutoNaoEncontradoException se não existir produto com o id
     */
    public Pedido adicionarItem(Long pedidoId, Long produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        Pedido pedido = buscarPorId(pedidoId);
        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new IllegalStateException("Só é possível adicionar itens a um pedido ABERTO (status atual: "
                    + pedido.getStatus() + ").");
        }

        Produto produto = produtoRepository.buscarPorId(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com id: " + produtoId));

        pedido.adicionarItem(new ItemPedido(produto.getId(), produto.getNome(), quantidade, produto.getPreco()));
        return pedidoRepository.salvar(pedido);
    }

    /**
     * Confirma um pedido ABERTO e dá baixa no estoque de todos os itens.
     * O estoque de todos os itens é verificado antes de qualquer baixa:
     * se algum faltar, nada é baixado e o pedido continua ABERTO.
     *
     * @param pedidoId id do pedido
     * @return o pedido confirmado
     * @throws IllegalArgumentException     se o pedido não existir
     * @throws IllegalStateException        se o pedido não estiver ABERTO ou não tiver itens
     * @throws EstoqueInsuficienteException se algum produto não tiver estoque suficiente
     */
    public Pedido confirmar(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);

        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new IllegalStateException("Só é possível confirmar um pedido ABERTO (status atual: "
                    + pedido.getStatus() + ").");
        }
        if (pedido.getItens().isEmpty()) {
            throw new IllegalStateException("Não é possível confirmar um pedido sem itens.");
        }

        // 1ª etapa: verifica o estoque de TODOS os itens, sem dar baixa em nada.
        // Se o mesmo produto aparecer em mais de um item, soma as quantidades.
        for (ItemPedido item : pedido.getItens()) {
            int quantidadeTotal = quantidadeTotalDoProduto(pedido, item.getProdutoId());
            if (!estoqueService.possuiEstoque(item.getProdutoId(), quantidadeTotal)) {
                throw new EstoqueInsuficienteException("Pedido não confirmado: estoque insuficiente para o produto '"
                        + item.getNomeProduto() + "'.");
            }
        }

        // 2ª etapa: todos têm estoque, então dá baixa na quantidade exata de cada item.
        for (ItemPedido item : pedido.getItens()) {
            estoqueService.darBaixa(item.getProdutoId(), item.getQuantidade());
        }

        pedido.setStatus(StatusPedido.CONFIRMADO);
        return pedidoRepository.salvar(pedido);
    }

    /**
     * Cancela um pedido. Se ele estava CONFIRMADO, as quantidades voltam ao estoque;
     * se estava ABERTO, o estoque não muda.
     *
     * @param pedidoId id do pedido
     * @return o pedido cancelado
     * @throws IllegalArgumentException se o pedido não existir
     * @throws IllegalStateException    se o pedido já estiver CANCELADO
     */
    public Pedido cancelar(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalStateException("O pedido " + pedidoId + " já está cancelado.");
        }

        if (pedido.getStatus() == StatusPedido.CONFIRMADO) {
            for (ItemPedido item : pedido.getItens()) {
                estoqueService.repor(item.getProdutoId(), item.getQuantidade());
            }
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return pedidoRepository.salvar(pedido);
    }

    // Soma as quantidades de todos os itens do pedido que são do mesmo produto.
    private int quantidadeTotalDoProduto(Pedido pedido, Long produtoId) {
        int total = 0;
        for (ItemPedido item : pedido.getItens()) {
            if (item.getProdutoId().equals(produtoId)) {
                total += item.getQuantidade();
            }
        }
        return total;
    }
}
