package br.unisul.lanchonete.repository;

import java.util.List;
import java.util.Optional;

import br.unisul.lanchonete.model.Pedido;

/**
 * Define as operações de acesso a dados dos pedidos.
 */
public interface PedidoRepository {

    /**
     * Salva um pedido novo ou atualiza um existente.
     *
     * @param pedido pedido a ser salvo
     * @return o pedido salvo, com o id preenchido
     */
    Pedido salvar(Pedido pedido);

    /**
     * Busca um pedido pelo id.
     *
     * @param id id do pedido
     * @return o pedido encontrado, ou vazio se não existir
     */
    Optional<Pedido> buscarPorId(Long id);

    /**
     * Lista todos os pedidos cadastrados.
     *
     * @return lista de pedidos, vazia se não houver nenhum
     */
    List<Pedido> listarTodos();
}
