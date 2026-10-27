package br.unisul.lanchonete.service;

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
