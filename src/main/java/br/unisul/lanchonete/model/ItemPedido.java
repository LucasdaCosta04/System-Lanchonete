package br.unisul.lanchonete.model;

import java.math.BigDecimal;

/**
 * Representa um item de um pedido.
 * O preço unitário é copiado do produto no momento em que o pedido é criado.
 */
public class ItemPedido {

    private final Long produtoId;
    private final String nomeProduto;
    private final int quantidade;
    private final BigDecimal precoUnitario;

    public ItemPedido(Long produtoId, String nomeProduto, int quantidade, BigDecimal precoUnitario) {
        if (produtoId == null) {
            throw new IllegalArgumentException("O id do produto é obrigatório.");
        }
        if (nomeProduto == null || nomeProduto.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (precoUnitario == null) {
            throw new IllegalArgumentException("O preço unitário é obrigatório.");
        }
        if (precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço unitário não pode ser negativo.");
        }

        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    // Subtotal do item: preço unitário x quantidade.
    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public String toString() {
        return "ItemPedido{produtoId=" + produtoId
                + ", nomeProduto='" + nomeProduto + "'"
                + ", quantidade=" + quantidade
                + ", precoUnitario=" + precoUnitario + "}";
    }
}
