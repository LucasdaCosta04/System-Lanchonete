package br.unisul.lanchonete.service;

import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.repository.ProdutoRepository;

/**
 * Contém as regras de negócio de controle de estoque dos produtos.
 */
public class EstoqueService {

    private final ProdutoRepository repository;

    public EstoqueService(ProdutoRepository repository) {
        this.repository = repository;
    }

    /**
     * Verifica se há saldo suficiente em estoque para a quantidade informada.
     *
     * @param produtoId id do produto
     * @param quantidade quantidade desejada
     * @return true se o estoque atual for suficiente
     * @throws IllegalArgumentException se a quantidade não for maior que zero
     * @throws ProdutoNaoEncontradoException se não existir produto com o id
     */
    public boolean possuiEstoque(Long produtoId, int quantidade) {
        validarQuantidade(quantidade);
        Produto produto = buscarProduto(produtoId);

        return produto.getQuantidadeEstoque() >= quantidade;
    }

    /**
     * Dá baixa no estoque do produto, subtraindo a quantidade informada.
     *
     * @param produtoId id do produto
     * @param quantidade quantidade a ser baixada
     * @throws IllegalArgumentException se a quantidade não for maior que zero
     * @throws ProdutoNaoEncontradoException se não existir produto com o id
     * @throws EstoqueInsuficienteException se o saldo em estoque for insuficiente
     */
    public void darBaixa(Long produtoId, int quantidade) {
        validarQuantidade(quantidade);
        Produto produto = buscarProduto(produtoId);

        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new EstoqueInsuficienteException("Estoque insuficiente para o produto '" + produto.getNome()
                    + "': saldo atual " + produto.getQuantidadeEstoque() + ", quantidade pedida " + quantidade + ".");
        }

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
        repository.salvar(produto);
    }

    /**
     * Repõe o estoque do produto, somando a quantidade informada.
     *
     * @param produtoId id do produto
     * @param quantidade quantidade a ser reposta
     * @throws IllegalArgumentException se a quantidade não for maior que zero
     * @throws ProdutoNaoEncontradoException se não existir produto com o id
     */
    public void repor(Long produtoId, int quantidade) {
        validarQuantidade(quantidade);
        Produto produto = buscarProduto(produtoId);

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + quantidade);
        repository.salvar(produto);
    }

    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }

    private Produto buscarProduto(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com id: " + id));
    }
}
