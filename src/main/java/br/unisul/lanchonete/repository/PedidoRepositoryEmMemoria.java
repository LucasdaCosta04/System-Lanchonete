package br.unisul.lanchonete.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import br.unisul.lanchonete.model.Pedido;

/**
 * Implementação do repositório de pedidos que guarda os dados em memória.
 */
public class PedidoRepositoryEmMemoria implements PedidoRepository {

    private final Map<Long, Pedido> pedidos = new LinkedHashMap<>();
    private final AtomicLong sequencia = new AtomicLong(1);

    @Override
    public Pedido salvar(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(sequencia.getAndIncrement());
        }
        pedidos.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    @Override
    public List<Pedido> listarTodos() {
        return new ArrayList<>(pedidos.values());
    }
}
