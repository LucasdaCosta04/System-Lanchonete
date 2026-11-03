package br.unisul.lanchonete.service;

/**
 * Lançada quando um produto procurado não existe no repositório.
 */
public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
