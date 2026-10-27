package br.unisul.lanchonete.service;

/**
 * Lançada quando um cliente procurado não existe no repositório.
 */
public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
