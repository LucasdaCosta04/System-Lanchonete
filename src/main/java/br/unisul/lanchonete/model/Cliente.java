package br.unisul.lanchonete.model;

import java.util.Objects;

/**
 * Representa um cliente da lanchonete.
 */
public class Cliente {

    private Long id;
    private String nome;
    private String cpf;
    private String telefone;

    public Cliente() {
    }

    public Cliente(String nome, String cpf, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    // Dois clientes são iguais quando têm o mesmo id.
    // Clientes ainda sem id (não salvos) só são iguais a si mesmos.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cliente outro = (Cliente) o;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Cliente{id=" + id
                + ", nome='" + nome + "'"
                + ", cpf='" + cpf + "'"
                + ", telefone='" + telefone + "'}";
    }
}
