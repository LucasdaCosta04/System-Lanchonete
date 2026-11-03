package br.unisul.lanchonete.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa um produto do cardápio da lanchonete.
 */
public class Produto {

    private Long id;
    private String nome;
    private String categoria;
    private BigDecimal preco;
    private int quantidadeEstoque;

    public Produto() {
    }

    public Produto(String nome, String categoria, BigDecimal preco, int quantidadeEstoque) {
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    // Dois produtos são iguais quando têm o mesmo id.
    // Produtos ainda sem id (não salvos) só são iguais a si mesmos.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Produto outro = (Produto) o;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Produto{id=" + id
                + ", nome='" + nome + "'"
                + ", categoria='" + categoria + "'"
                + ", preco=" + preco
                + ", quantidadeEstoque=" + quantidadeEstoque + "}";
    }
}
