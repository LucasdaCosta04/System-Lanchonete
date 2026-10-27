package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
