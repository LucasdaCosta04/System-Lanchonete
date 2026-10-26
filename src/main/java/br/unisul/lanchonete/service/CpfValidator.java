package br.unisul.lanchonete.service;

/**
 * Valida números de CPF, aceitando com ou sem pontos e traço.
 */
public final class CpfValidator {

    private CpfValidator() {
    }

    public static boolean isValido(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }

        String numeros = cpf.replace(".", "").replace("-", "");

        if (!numeros.matches("\\d{11}")) {
            return false;
        }

        if (todosDigitosIguais(numeros)) {
            return false;
        }

        int primeiroDigito = calcularDigito(numeros, 9);
        int segundoDigito = calcularDigito(numeros, 10);

        return primeiroDigito == Character.getNumericValue(numeros.charAt(9))
                && segundoDigito == Character.getNumericValue(numeros.charAt(10));
    }

    private static boolean todosDigitosIguais(String numeros) {
        for (int i = 1; i < numeros.length(); i++) {
            if (numeros.charAt(i) != numeros.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    // Multiplica os primeiros "quantidade" dígitos por pesos decrescentes
    // (começando em quantidade + 1 e terminando em 2) e calcula o dígito pelo resto da divisão por 11.
    private static int calcularDigito(String numeros, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;

        for (int i = 0; i < quantidade; i++) {
            int digito = Character.getNumericValue(numeros.charAt(i));
            soma += digito * peso;
            peso--;
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
