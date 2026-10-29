package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    private static final String NOME = "X-Burguer";
    private static final String CATEGORIA = "Lanche";
    private static final BigDecimal PRECO = new BigDecimal("18.00");
    private static final int ESTOQUE = 10;

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    @Test
    void deveCadastrarProdutoValidoERetornarProdutoSalvo() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        Produto salvo = new Produto(NOME, CATEGORIA, PRECO, ESTOQUE);
        salvo.setId(1L);
        when(repository.salvar(produto)).thenReturn(salvo);

        Produto resultado = service.cadastrar(produto);

        assertSame(salvo, resultado);
        verify(repository, times(1)).salvar(produto);
    }

    @Test
    void deveRejeitarProdutoNulo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(null));

        assertEquals("O produto não pode ser nulo.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void deveRejeitarNomeNuloOuEmBranco(String nome) {
        Produto produto = new Produto(nome, CATEGORIA, PRECO, ESTOQUE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(produto));

        assertEquals("O nome do produto é obrigatório.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void deveRejeitarCategoriaNulaOuEmBranco(String categoria) {
        Produto produto = new Produto(NOME, categoria, PRECO, ESTOQUE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(produto));

        assertEquals("A categoria do produto é obrigatória.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarPrecoNulo() {
        Produto produto = new Produto(NOME, CATEGORIA, null, ESTOQUE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(produto));

        assertEquals("O preço do produto é obrigatório.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarPrecoZero() {
        Produto produto = new Produto(NOME, CATEGORIA, BigDecimal.ZERO, ESTOQUE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(produto));

        assertEquals("O preço do produto deve ser maior que zero.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarPrecoNegativo() {
        Produto produto = new Produto(NOME, CATEGORIA, new BigDecimal("-1.00"), ESTOQUE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(produto));

        assertEquals("O preço do produto deve ser maior que zero.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, -1);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(produto));

        assertEquals("A quantidade em estoque não pode ser negativa.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void devePermitirEstoqueZero() {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, 0);
        Produto salvo = new Produto(NOME, CATEGORIA, PRECO, 0);
        salvo.setId(1L);
        when(repository.salvar(produto)).thenReturn(salvo);

        Produto resultado = service.cadastrar(produto);

        assertSame(salvo, resultado);
        verify(repository, times(1)).salvar(produto);
    }
}
