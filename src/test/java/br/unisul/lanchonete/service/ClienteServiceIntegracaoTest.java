package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.repository.ClienteRepositoryEmMemoria;

/**
 * Testa o ClienteService junto com o repositório em memória real, sem mocks.
 * Os CPFs válidos usados aqui são fictícios, calculados a partir das bases
 * sequenciais 123456789 e 987654321 aplicando a regra dos dígitos verificadores.
 */
@Tag("integracao")
@DisplayName("Integração: ClienteService + ClienteRepositoryEmMemoria")
class ClienteServiceIntegracaoTest {

    private static final String CPF_VALIDO = "12345678909";
    private static final String CPF_VALIDO_COM_PONTUACAO = "123.456.789-09";
    private static final String OUTRO_CPF_VALIDO = "98765432100";
    private static final String TELEFONE = "(48) 99999-0000";
    private static final String OUTRO_TELEFONE = "(48) 98888-0000";

    private ClienteRepositoryEmMemoria repository;
    private ClienteService service;

    @BeforeEach
    void criarServicoComRepositorioReal() {
        repository = new ClienteRepositoryEmMemoria();
        service = new ClienteService(repository);
    }

    @Test
    void deveCadastrarClienteEEncontrarPorIdComOsMesmosDados() {
        Cliente salvo = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO_COM_PONTUACAO, TELEFONE));

        Cliente encontrado = service.buscarPorId(salvo.getId());

        assertNotNull(salvo.getId());
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Maria Silva", encontrado.getNome());
        assertEquals(CPF_VALIDO, encontrado.getCpf());
        assertEquals(TELEFONE, encontrado.getTelefone());
    }

    @Test
    void deveEncontrarPorCpfComESemPontuacaoAposCadastrarComCpfPontuado() {
        Cliente salvo = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO_COM_PONTUACAO, TELEFONE));

        Cliente semPontuacao = service.buscarPorCpf(CPF_VALIDO);
        Cliente comPontuacao = service.buscarPorCpf(CPF_VALIDO_COM_PONTUACAO);

        assertEquals(salvo.getId(), semPontuacao.getId());
        assertEquals(salvo.getId(), comPontuacao.getId());
    }

    @Test
    void deveCadastrarDoisClientesComIdsDiferentesEListarNaOrdemDeCadastro() {
        Cliente maria = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));
        Cliente joao = service.cadastrar(new Cliente("João Souza", OUTRO_CPF_VALIDO, OUTRO_TELEFONE));

        List<Cliente> clientes = service.listarTodos();

        assertNotEquals(maria.getId(), joao.getId());
        assertEquals(2, clientes.size());
        assertEquals(maria.getId(), clientes.get(0).getId());
        assertEquals(joao.getId(), clientes.get(1).getId());
    }

    @Test
    void naoDeveCadastrarSegundoClienteComMesmoCpf() {
        service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));
        Cliente duplicado = new Cliente("João Souza", CPF_VALIDO_COM_PONTUACAO, OUTRO_TELEFONE);

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(duplicado));

        List<Cliente> clientes = service.listarTodos();
        assertEquals(1, clientes.size());
        assertEquals("Maria Silva", clientes.get(0).getNome());
    }

    @ParameterizedTest
    @MethodSource("clientesInvalidos")
    void cadastroInvalidoNaoDeveSalvarNadaNoRepositorio(Cliente invalido) {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(invalido));

        assertTrue(repository.listarTodos().isEmpty());
    }

    @Test
    void buscarIdInexistenteDeveLancarClienteNaoEncontradoException() {
        assertThrows(ClienteNaoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void deveAtualizarNomeETelefoneSemCriarOutroCliente() {
        Cliente salvo = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));

        service.atualizar(salvo.getId(), new Cliente("Maria Souza", CPF_VALIDO, OUTRO_TELEFONE));

        Cliente encontrado = service.buscarPorId(salvo.getId());
        assertEquals("Maria Souza", encontrado.getNome());
        assertEquals(CPF_VALIDO, encontrado.getCpf());
        assertEquals(OUTRO_TELEFONE, encontrado.getTelefone());
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    void deveAtualizarCpfEEncontrarSoPeloCpfNovo() {
        Cliente salvo = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));

        service.atualizar(salvo.getId(), new Cliente("Maria Silva", OUTRO_CPF_VALIDO, TELEFONE));

        assertEquals(salvo.getId(), service.buscarPorCpf(OUTRO_CPF_VALIDO).getId());
        assertThrows(ClienteNaoEncontradoException.class, () -> service.buscarPorCpf(CPF_VALIDO));
    }

    @Test
    void naoDeveAtualizarClienteComCpfDeOutroCliente() {
        Cliente maria = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));
        service.cadastrar(new Cliente("João Souza", OUTRO_CPF_VALIDO, OUTRO_TELEFONE));
        Cliente dados = new Cliente("Maria Souza", OUTRO_CPF_VALIDO, OUTRO_TELEFONE);

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(maria.getId(), dados));

        Cliente encontrado = service.buscarPorId(maria.getId());
        assertEquals("Maria Silva", encontrado.getNome());
        assertEquals(CPF_VALIDO, encontrado.getCpf());
        assertEquals(TELEFONE, encontrado.getTelefone());
    }

    @ParameterizedTest
    @MethodSource("clientesInvalidos")
    void atualizacaoInvalidaNaoDeveAlterarClienteSalvo(Cliente invalido) {
        Cliente salvo = service.cadastrar(new Cliente("Maria Silva", OUTRO_CPF_VALIDO, OUTRO_TELEFONE));

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(salvo.getId(), invalido));

        Cliente encontrado = service.buscarPorId(salvo.getId());
        assertEquals("Maria Silva", encontrado.getNome());
        assertEquals(OUTRO_CPF_VALIDO, encontrado.getCpf());
        assertEquals(OUTRO_TELEFONE, encontrado.getTelefone());
    }

    @Test
    void excluirClienteDeveRemoverDoRepositorio() {
        Cliente salvo = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));

        service.excluir(salvo.getId());

        assertThrows(ClienteNaoEncontradoException.class, () -> service.buscarPorId(salvo.getId()));
        assertTrue(service.listarTodos().stream().noneMatch(c -> c.getId().equals(salvo.getId())));
    }

    @Test
    void excluirUmClienteNaoDeveAfetarOOutro() {
        Cliente maria = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));
        Cliente joao = service.cadastrar(new Cliente("João Souza", OUTRO_CPF_VALIDO, OUTRO_TELEFONE));

        service.excluir(maria.getId());

        List<Cliente> clientes = service.listarTodos();
        assertEquals(1, clientes.size());
        assertEquals(joao.getId(), clientes.get(0).getId());

        Cliente encontrado = service.buscarPorId(joao.getId());
        assertEquals("João Souza", encontrado.getNome());
        assertEquals(OUTRO_CPF_VALIDO, encontrado.getCpf());
        assertEquals(OUTRO_TELEFONE, encontrado.getTelefone());
    }

    @Test
    void excluirIdInexistenteDeveLancarExcecaoSemRemoverNada() {
        Cliente salvo = service.cadastrar(new Cliente("Maria Silva", CPF_VALIDO, TELEFONE));

        assertThrows(ClienteNaoEncontradoException.class, () -> service.excluir(99L));

        List<Cliente> clientes = service.listarTodos();
        assertEquals(1, clientes.size());
        assertEquals(salvo.getId(), clientes.get(0).getId());
    }

    static Stream<Cliente> clientesInvalidos() {
        return Stream.of(
                new Cliente(" ", CPF_VALIDO, TELEFONE),
                new Cliente("Maria Silva", "12345678900", TELEFONE),
                new Cliente("Maria Silva", CPF_VALIDO, null));
    }
}
