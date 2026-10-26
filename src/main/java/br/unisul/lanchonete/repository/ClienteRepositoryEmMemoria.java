package br.unisul.lanchonete.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import br.unisul.lanchonete.model.Cliente;

/**
 * Implementação do repositório de clientes que guarda os dados em memória.
 */
public class ClienteRepositoryEmMemoria implements ClienteRepository {

    private final Map<Long, Cliente> clientes = new LinkedHashMap<>();
    private final AtomicLong sequencia = new AtomicLong(1);

    @Override
    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(sequencia.getAndIncrement());
        }
        clientes.put(cliente.getId(), cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return Optional.ofNullable(clientes.get(id));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        String cpfProcurado = somenteNumeros(cpf);

        for (Cliente cliente : clientes.values()) {
            if (cliente.getCpf() != null && somenteNumeros(cliente.getCpf()).equals(cpfProcurado)) {
                return Optional.of(cliente);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Cliente> listarTodos() {
        return new ArrayList<>(clientes.values());
    }

    @Override
    public void excluir(Long id) {
        clientes.remove(id);
    }

    private String somenteNumeros(String cpf) {
        return cpf.replace(".", "").replace("-", "");
    }
}
