package br.unisul.lanchonete.service;

import java.math.BigDecimal;
import java.util.List;

import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.repository.ProdutoRepository;

/**
 * Contém as regras de negócio do cadastro de produtos.
 */
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida e cadastra um novo produto.
     *
     * @param produto produto a ser cadastrado
     * @return o produto salvo, com o id preenchido
     * @throws IllegalArgumentException se algum dado do produto for inválido
     */
    public Produto cadastrar(Produto produto) {
        validar(produto);

        return repository.salvar(produto);
    }

    /**
     * Busca um produto pelo id.
     *
     * @param id id do produto
     * @return o produto encontrado
     * @throws ProdutoNaoEncontradoException se não existir produto com o id
     */
    public Produto buscarPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com id: " + id));
    }

    /**
     * Lista todos os produtos cadastrados.
     *
     * @return lista de produtos, vazia se não houver nenhum
     */
    public List<Produto> listarTodos() {
        return repository.listarTodos();
    }

    private void validar(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo.");
        }
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (produto.getCategoria() == null || produto.getCategoria().isBlank()) {
            throw new IllegalArgumentException("A categoria do produto é obrigatória.");
        }
        if (produto.getPreco() == null) {
            throw new IllegalArgumentException("O preço do produto é obrigatório.");
        }
        if (produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço do produto deve ser maior que zero.");
        }
        if (produto.getQuantidadeEstoque() < 0) {
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
        }
    }
}
