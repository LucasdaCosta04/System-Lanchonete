package br.unisul.lanchonete.repository;

import java.util.List;
import java.util.Optional;

import br.unisul.lanchonete.model.Produto;

/**
 * Define as operações de acesso a dados dos produtos.
 */
public interface ProdutoRepository {

    /**
     * Salva um produto novo ou atualiza um existente.
     *
     * @param produto produto a ser salvo
     * @return o produto salvo, com o id preenchido
     */
    Produto salvar(Produto produto);

    /**
     * Busca um produto pelo id.
     *
     * @param id id do produto
     * @return o produto encontrado, ou vazio se não existir
     */
    Optional<Produto> buscarPorId(Long id);

    /**
     * Lista todos os produtos cadastrados.
     *
     * @return lista de produtos, vazia se não houver nenhum
     */
    List<Produto> listarTodos();

    /**
     * Exclui o produto com o id informado.
     *
     * @param id id do produto a ser excluído
     */
    void excluir(Long id);
}
