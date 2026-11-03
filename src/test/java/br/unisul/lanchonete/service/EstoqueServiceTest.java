package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    private static final String NOME = "X-Burguer";
    private static final String CATEGORIA = "Lanche";
    private static final BigDecimal PRECO = new BigDecimal("18.00");
    private static final Long PRODUTO_ID = 1L;

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private EstoqueService service;

    private Produto criarProduto(int quantidadeEstoque) {
        Produto produto = new Produto(NOME, CATEGORIA, PRECO, quantidadeEstoque);
        produto.setId(PRODUTO_ID);
        return produto;
    }

    @Test
    void devePossuirEstoqueQuandoSaldoForMaiorQueQuantidadePedida() {
        Produto produto = criarProduto(10);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));

        boolean resultado = service.possuiEstoque(PRODUTO_ID, 3);

        assertTrue(resultado);
    }

    @Test
    void devePossuirEstoqueQuandoQuantidadeForExatamenteIgualAoSaldo() {
        Produto produto = criarProduto(10);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));

        boolean resultado = service.possuiEstoque(PRODUTO_ID, 10);

        assertTrue(resultado);
    }

    @Test
    void naoDevePossuirEstoqueQuandoQuantidadeForMaiorQueSaldo() {
        Produto produto = criarProduto(10);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));

        boolean resultado = service.possuiEstoque(PRODUTO_ID, 11);

        assertFalse(resultado);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void devePossuirEstoqueDeveRejeitarQuantidadeZeroOuNegativa(int quantidade) {
        assertThrows(IllegalArgumentException.class, () -> service.possuiEstoque(PRODUTO_ID, quantidade));
    }

    @Test
    void devePossuirEstoqueDeveLancarExcecaoQuandoProdutoNaoExistir() {
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> service.possuiEstoque(PRODUTO_ID, 1));
    }

    @Test
    void deveDarBaixaReduzindoEstoqueNoValorExatoEChamarSalvar() {
        Produto produto = criarProduto(10);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);

        service.darBaixa(PRODUTO_ID, 3);

        verify(repository, times(1)).salvar(captor.capture());
        assertEquals(7, captor.getValue().getQuantidadeEstoque());
    }

    @Test
    void deveDarBaixaComQuantidadeIgualAoSaldoDeixandoEstoqueEmZero() {
        Produto produto = criarProduto(10);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);

        service.darBaixa(PRODUTO_ID, 10);

        verify(repository, times(1)).salvar(captor.capture());
        assertEquals(0, captor.getValue().getQuantidadeEstoque());
    }

    @Test
    void deveDarBaixaLancarExcecaoQuandoSaldoForInsuficiente() {
        Produto produto = criarProduto(5);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));

        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
                () -> service.darBaixa(PRODUTO_ID, 6));

        assertTrue(erro.getMessage().contains(NOME));
        assertEquals(5, produto.getQuantidadeEstoque());
        verify(repository, never()).salvar(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void deveDarBaixaRejeitarQuantidadeZeroOuNegativaSemChamarSalvar(int quantidade) {
        assertThrows(IllegalArgumentException.class, () -> service.darBaixa(PRODUTO_ID, quantidade));

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveDarBaixaLancarExcecaoQuandoProdutoNaoExistirSemChamarSalvar() {
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> service.darBaixa(PRODUTO_ID, 1));

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveReporSomandoQuantidadeAoSaldoEChamarSalvar() {
        Produto produto = criarProduto(10);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);

        service.repor(PRODUTO_ID, 5);

        verify(repository, times(1)).salvar(captor.capture());
        assertEquals(15, captor.getValue().getQuantidadeEstoque());
    }

    @Test
    void deveReporEmProdutoComEstoqueZeroDeixandoSaldoIgualAQuantidadeReposta() {
        Produto produto = criarProduto(0);
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.of(produto));
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);

        service.repor(PRODUTO_ID, 8);

        verify(repository, times(1)).salvar(captor.capture());
        assertEquals(8, captor.getValue().getQuantidadeEstoque());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void deveReporRejeitarQuantidadeZeroOuNegativaSemChamarSalvarNemAlterarSaldo(int quantidade) {
        Produto produto = criarProduto(10);

        assertThrows(IllegalArgumentException.class, () -> service.repor(PRODUTO_ID, quantidade));

        assertEquals(10, produto.getQuantidadeEstoque());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveReporLancarExcecaoQuandoProdutoNaoExistirSemChamarSalvar() {
        when(repository.buscarPorId(PRODUTO_ID)).thenReturn(Optional.empty());

        ProdutoNaoEncontradoException erro = assertThrows(ProdutoNaoEncontradoException.class,
                () -> service.repor(PRODUTO_ID, 5));

        assertTrue(erro.getMessage().contains(String.valueOf(PRODUTO_ID)));
        verify(repository, never()).salvar(any());
    }
}
