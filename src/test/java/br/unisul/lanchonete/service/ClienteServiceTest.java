package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.repository.ClienteRepository;

/**
 * Os CPFs válidos usados aqui são fictícios, calculados a partir da base
 * sequencial 123456789 aplicando a regra dos dígitos verificadores.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private static final String CPF_VALIDO = "12345678909";
    private static final String CPF_VALIDO_COM_PONTUACAO = "123.456.789-09";
    private static final String TELEFONE = "(48) 99999-0000";
    private static final String OUTRO_CPF_VALIDO = "98765432100";
    private static final String OUTRO_CPF_VALIDO_COM_PONTUACAO = "987.654.321-00";

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    @Test
    void deveCadastrarClienteValidoERetornarClienteSalvo() {
        Cliente cliente = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        Cliente salvo = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        salvo.setId(1L);
        when(repository.salvar(cliente)).thenReturn(salvo);

        Cliente resultado = service.cadastrar(cliente);

        assertSame(salvo, resultado);
        verify(repository, times(1)).salvar(cliente);
    }

    @Test
    void deveSalvarCpfApenasComDigitos() {
        Cliente cliente = new Cliente("Maria Silva", CPF_VALIDO_COM_PONTUACAO, TELEFONE);
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);

        service.cadastrar(cliente);

        verify(repository).salvar(captor.capture());
        assertEquals(CPF_VALIDO, captor.getValue().getCpf());
    }

    @Test
    void deveRejeitarClienteNulo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(null));

        assertEquals("O cliente não pode ser nulo.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void deveRejeitarNomeNuloOuEmBranco(String nome) {
        Cliente cliente = new Cliente(nome, CPF_VALIDO, TELEFONE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(cliente));

        assertEquals("O nome do cliente é obrigatório.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarCpfInvalido() {
        Cliente cliente = new Cliente("Maria Silva", "12345678900", TELEFONE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(cliente));

        assertEquals("O CPF informado é inválido.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void deveRejeitarTelefoneNuloOuEmBranco(String telefone) {
        Cliente cliente = new Cliente("Maria Silva", CPF_VALIDO, telefone);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(cliente));

        assertEquals("O telefone do cliente é obrigatório.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarCpfJaCadastrado() {
        Cliente existente = new Cliente("João Souza", CPF_VALIDO, TELEFONE);
        existente.setId(1L);
        when(repository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.of(existente));
        Cliente cliente = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(cliente));

        assertEquals("Já existe um cliente cadastrado com este CPF", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveConsultarCpfDuplicadoApenasComDigitos() {
        Cliente cliente = new Cliente("Maria Silva", CPF_VALIDO_COM_PONTUACAO, TELEFONE);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        service.cadastrar(cliente);

        verify(repository).buscarPorCpf(captor.capture());
        assertEquals(CPF_VALIDO, captor.getValue());
    }

    @Test
    void deveCadastrarQuandoCpfNaoExiste() {
        Cliente cliente = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        Cliente salvo = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        salvo.setId(1L);
        when(repository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.empty());
        when(repository.salvar(cliente)).thenReturn(salvo);

        Cliente resultado = service.cadastrar(cliente);

        assertSame(salvo, resultado);
        verify(repository, times(1)).salvar(cliente);
    }

    @Test
    void deveBuscarClienteExistentePorId() {
        Cliente existente = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        existente.setId(1L);
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(existente));

        Cliente resultado = service.buscarPorId(1L);

        assertSame(existente, resultado);
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());

        ClienteNaoEncontradoException erro = assertThrows(ClienteNaoEncontradoException.class,
                () -> service.buscarPorId(99L));

        assertTrue(erro.getMessage().contains("99"));
    }

    @Test
    void deveBuscarPorCpfComPontuacaoUsandoApenasDigitos() {
        Cliente existente = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        existente.setId(1L);
        when(repository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.of(existente));

        Cliente resultado = service.buscarPorCpf(CPF_VALIDO_COM_PONTUACAO);

        assertSame(existente, resultado);
        verify(repository).buscarPorCpf(CPF_VALIDO);
    }

    @Test
    void deveLancarExcecaoAoBuscarCpfInexistente() {
        when(repository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class,
                () -> service.buscarPorCpf(CPF_VALIDO));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        when(repository.listarTodos()).thenReturn(List.of());

        List<Cliente> resultado = service.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveListarTodosOsClientesDoRepositorio() {
        Cliente maria = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        maria.setId(1L);
        Cliente joao = new Cliente("João Souza", "98765432100", TELEFONE);
        joao.setId(2L);
        when(repository.listarTodos()).thenReturn(List.of(maria, joao));

        List<Cliente> resultado = service.listarTodos();

        assertEquals(List.of(maria, joao), resultado);
    }

    // Testes do método atualizar

    // Configura o mock para encontrar a Maria (id 1) ao buscar por id.
    private Cliente clienteExistenteNoRepositorio() {
        Cliente existente = new Cliente("Maria Silva", CPF_VALIDO, TELEFONE);
        existente.setId(1L);
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(existente));
        return existente;
    }

    @Test
    void deveAtualizarNomeCpfETelefoneDoClienteExistente() {
        Cliente existente = clienteExistenteNoRepositorio();
        when(repository.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        Cliente dados = new Cliente("Maria Souza", OUTRO_CPF_VALIDO, "(48) 98888-1111");

        Cliente resultado = service.atualizar(1L, dados);

        assertSame(existente, resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Maria Souza", resultado.getNome());
        assertEquals(OUTRO_CPF_VALIDO, resultado.getCpf());
        assertEquals("(48) 98888-1111", resultado.getTelefone());
        verify(repository, times(1)).salvar(existente);
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());
        Cliente dados = new Cliente("Maria Souza", CPF_VALIDO, TELEFONE);

        assertThrows(ClienteNaoEncontradoException.class,
                () -> service.atualizar(99L, dados));

        verify(repository, never()).salvar(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void deveRejeitarAtualizacaoComNomeNuloOuEmBranco(String nome) {
        clienteExistenteNoRepositorio();
        Cliente dados = new Cliente(nome, CPF_VALIDO, TELEFONE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.atualizar(1L, dados));

        assertEquals("O nome do cliente é obrigatório.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarAtualizacaoComCpfInvalido() {
        clienteExistenteNoRepositorio();
        Cliente dados = new Cliente("Maria Silva", "12345678900", TELEFONE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.atualizar(1L, dados));

        assertEquals("O CPF informado é inválido.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void deveRejeitarAtualizacaoComTelefoneNuloOuEmBranco(String telefone) {
        clienteExistenteNoRepositorio();
        Cliente dados = new Cliente("Maria Silva", CPF_VALIDO, telefone);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.atualizar(1L, dados));

        assertEquals("O telefone do cliente é obrigatório.", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRejeitarAtualizacaoComCpfDeOutroCliente() {
        clienteExistenteNoRepositorio();
        Cliente outro = new Cliente("João Souza", OUTRO_CPF_VALIDO, TELEFONE);
        outro.setId(2L);
        when(repository.buscarPorCpf(OUTRO_CPF_VALIDO)).thenReturn(Optional.of(outro));
        Cliente dados = new Cliente("Maria Silva", OUTRO_CPF_VALIDO, TELEFONE);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.atualizar(1L, dados));

        assertEquals("Já existe um cliente cadastrado com este CPF", erro.getMessage());
        verify(repository, never()).salvar(any());
    }

    @Test
    void devePermitirAtualizacaoMantendoOProprioCpf() {
        Cliente existente = clienteExistenteNoRepositorio();
        when(repository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.of(existente));
        when(repository.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        Cliente dados = new Cliente("Maria Souza", CPF_VALIDO, TELEFONE);

        Cliente resultado = service.atualizar(1L, dados);

        assertEquals("Maria Souza", resultado.getNome());
        assertEquals(CPF_VALIDO, resultado.getCpf());
        verify(repository, times(1)).salvar(existente);
    }

    @Test
    void deveSalvarCpfApenasComDigitosAoAtualizar() {
        clienteExistenteNoRepositorio();
        Cliente dados = new Cliente("Maria Silva", OUTRO_CPF_VALIDO_COM_PONTUACAO, TELEFONE);
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);

        service.atualizar(1L, dados);

        verify(repository).salvar(captor.capture());
        assertEquals(OUTRO_CPF_VALIDO, captor.getValue().getCpf());
    }

    // Testes do método excluir

    @Test
    void deveExcluirClienteExistente() {
        clienteExistenteNoRepositorio();

        service.excluir(1L);

        verify(repository, times(1)).excluir(1L);
    }

    @Test
    void deveLancarExcecaoAoExcluirIdInexistente() {
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class,
                () -> service.excluir(99L));

        verify(repository, never()).excluir(any());
    }
}
