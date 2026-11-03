package br.unisul.lanchonete.service;

/**
 * Lançada quando não há saldo suficiente em estoque para uma operação.
 */
public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
