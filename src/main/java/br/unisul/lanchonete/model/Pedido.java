package br.unisul.lanchonete.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa um pedido feito por um cliente da lanchonete.
 * Todo pedido começa com status ABERTO.
 */
public class Pedido {

    private Long id;
    private final Long clienteId;
    private final List<ItemPedido> itens = new ArrayList<>();
    private StatusPedido status = StatusPedido.ABERTO;

    public Pedido(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    // Lista somente leitura: novos itens entram apenas por adicionarItem.
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public void adicionarItem(ItemPedido item) {
        if (item == null) {
            throw new IllegalArgumentException("O item do pedido é obrigatório.");
        }
        itens.add(item);
    }

    // Total do pedido: soma dos subtotais dos itens (zero se não houver itens).
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    // Dois pedidos são iguais quando têm o mesmo id.
    // Pedidos ainda sem id (não salvos) só são iguais a si mesmos.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pedido outro = (Pedido) o;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Pedido{id=" + id
                + ", clienteId=" + clienteId
                + ", status=" + status
                + ", itens=" + itens
                + ", total=" + getTotal() + "}";
    }
}
