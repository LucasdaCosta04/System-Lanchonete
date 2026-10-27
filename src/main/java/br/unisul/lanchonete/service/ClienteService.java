package br.unisul.lanchonete.service;

import java.util.List;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.repository.ClienteRepository;

/**
 * Contém as regras de negócio do cadastro de clientes.
 */
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida e cadastra um novo cliente.
     *
     * @param cliente cliente a ser cadastrado
     * @return o cliente salvo, com o id preenchido
     * @throws IllegalArgumentException se algum dado do cliente for inválido
     */
    public Cliente cadastrar(Cliente cliente) {
        validar(cliente);

        // Guarda o CPF só com dígitos, sem pontos e traço.
        cliente.setCpf(cliente.getCpf().replaceAll("\\D", ""));

        if (repository.buscarPorCpf(cliente.getCpf()).isPresent()) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este CPF");
        }

        return repository.salvar(cliente);
    }

    /**
     * Busca um cliente pelo id.
     *
     * @param id id do cliente
     * @return o cliente encontrado
     * @throws ClienteNaoEncontradoException se não existir cliente com o id
     */
    public Cliente buscarPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado: id " + id));
    }

    /**
     * Busca um cliente pelo CPF, aceitando com ou sem pontos e traço.
     *
     * @param cpf CPF do cliente
     * @return o cliente encontrado
     * @throws ClienteNaoEncontradoException se não existir cliente com o CPF
     */
    public Cliente buscarPorCpf(String cpf) {
        String numeros = cpf == null ? "" : cpf.replaceAll("\\D", "");

        return repository.buscarPorCpf(numeros)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado: CPF " + numeros));
    }

    /**
     * Lista todos os clientes cadastrados.
     *
     * @return lista de clientes, vazia se não houver nenhum
     */
    public List<Cliente> listarTodos() {
        return repository.listarTodos();
    }

    private void validar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O cliente não pode ser nulo.");
        }
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (!CpfValidator.isValido(cliente.getCpf())) {
            throw new IllegalArgumentException("O CPF informado é inválido.");
        }
        if (cliente.getTelefone() == null || cliente.getTelefone().isBlank()) {
            throw new IllegalArgumentException("O telefone do cliente é obrigatório.");
        }
    }
}
