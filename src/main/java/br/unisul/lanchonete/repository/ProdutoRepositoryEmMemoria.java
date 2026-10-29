package br.unisul.lanchonete.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import br.unisul.lanchonete.model.Produto;

/**
 * Implementação do repositório de produtos que guarda os dados em memória.
 */
public class ProdutoRepositoryEmMemoria implements ProdutoRepository {

    private final Map<Long, Produto> produtos = new LinkedHashMap<>();
    private final AtomicLong sequencia = new AtomicLong(1);

    @Override
    public Produto salvar(Produto produto) {
        if (produto.getId() == null) {
            produto.setId(sequencia.getAndIncrement());
        }
        produtos.put(produto.getId(), produto);
        return produto;
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return Optional.ofNullable(produtos.get(id));
    }

    @Override
    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos.values());
    }

    @Override
    public void excluir(Long id) {
        produtos.remove(id);
    }
}
