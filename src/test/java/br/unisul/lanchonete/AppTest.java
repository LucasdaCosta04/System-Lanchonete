package br.unisul.lanchonete;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void mainDeveImprimirTituloDoSistema() {
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));

        try {
            App.main(new String[] {});
        } finally {
            System.setOut(saidaOriginal);
        }

        assertEquals("Sistema de Lanchonete", saida.toString().trim());
    }
}
