package br.unisul.lanchonete.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Os CPFs válidos usados aqui foram calculados a partir de bases sequenciais
 * (123456789 e 987654321) aplicando a regra dos dígitos verificadores.
 */
class CpfValidatorTest {

    @Test
    void deveAceitarCpfValidoSemPontuacao() {
        assertTrue(CpfValidator.isValido("12345678909"));
        assertTrue(CpfValidator.isValido("98765432100"));
    }

    @Test
    void deveAceitarCpfValidoComPontuacao() {
        assertTrue(CpfValidator.isValido("123.456.789-09"));
        assertTrue(CpfValidator.isValido("987.654.321-00"));
    }

    @Test
    void deveRejeitarCpfComPrimeiroDigitoVerificadorErrado() {
        assertFalse(CpfValidator.isValido("12345678919"));
    }

    @Test
    void deveRejeitarCpfComSegundoDigitoVerificadorErrado() {
        assertFalse(CpfValidator.isValido("12345678908"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "55555555555", "99999999999", "222.222.222-22"})
    void deveRejeitarCpfComTodosOsDigitosIguais(String cpf) {
        assertFalse(CpfValidator.isValido(cpf));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRejeitarCpfNuloOuVazio(String cpf) {
        assertFalse(CpfValidator.isValido(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "123456789091", "123"})
    void deveRejeitarCpfComTamanhoDiferenteDeOnzeDigitos(String cpf) {
        assertFalse(CpfValidator.isValido(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890a", "abc.def.ghi-jk"})
    void deveRejeitarCpfComLetras(String cpf) {
        assertFalse(CpfValidator.isValido(cpf));
    }
}
