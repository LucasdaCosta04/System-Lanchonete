package br.unisul.lanchonete.model;

/**
 * Status possíveis de um pedido.
 * Transições permitidas: ABERTO -> CONFIRMADO; ABERTO ou CONFIRMADO -> CANCELADO.
 */
public enum StatusPedido {
    ABERTO,
    CONFIRMADO,
    CANCELADO
}
