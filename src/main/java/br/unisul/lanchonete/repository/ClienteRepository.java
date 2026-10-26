package br.unisul.lanchonete.repository;

import java.util.List;
import java.util.Optional;

import br.unisul.lanchonete.model.Cliente;

/**
 * Define as operações de acesso a dados dos clientes.
 */
public interface ClienteRepository {

    /**
     * Salva um cliente novo ou atualiza um existente.
     *
     * @param cliente cliente a ser salvo
     * @return o cliente salvo, com o id preenchido
     */
    Cliente salvar(Cliente cliente);

    /**
     * Busca um cliente pelo id.
     *
     * @param id id do cliente
     * @return o cliente encontrado, ou vazio se não existir
     */
    Optional<Cliente> buscarPorId(Long id);

    /**
     * Busca um cliente pelo CPF.
     *
     * @param cpf CPF do cliente
     * @return o cliente encontrado, ou vazio se não existir
     */
    Optional<Cliente> buscarPorCpf(String cpf);

    /**
     * Lista todos os clientes cadastrados.
     *
     * @return lista de clientes, vazia se não houver nenhum
     */
    List<Cliente> listarTodos();

    /**
     * Exclui o cliente com o id informado.
     *
     * @param id id do cliente a ser excluído
     */
    void excluir(Long id);
}
