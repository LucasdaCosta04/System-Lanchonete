package br.unisul.lanchonete.web;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

@ExtendWith(MockitoExtension.class)
class ArquivoEstaticoHandlerTest {

    @Mock
    private HttpExchange troca;

    private final Headers cabecalhos = new Headers();
    private final ByteArrayOutputStream corpoDaResposta = new ByteArrayOutputStream();

    private final ArquivoEstaticoHandler handler = new ArquivoEstaticoHandler();

    private void prepararRequisicao(String metodo) {
        when(troca.getRequestMethod()).thenReturn(metodo);
        when(troca.getResponseHeaders()).thenReturn(cabecalhos);
        when(troca.getResponseBody()).thenReturn(corpoDaResposta);
    }

    private void prepararGet(String caminho) {
        prepararRequisicao("GET");
        when(troca.getRequestURI()).thenReturn(URI.create(caminho));
    }

    private String resposta() {
        return corpoDaResposta.toString(StandardCharsets.UTF_8);
    }

    private static String lerRecurso(String nome) throws IOException {
        try (InputStream arquivo = ArquivoEstaticoHandlerTest.class.getResourceAsStream("/web/" + nome)) {
            assertNotNull(arquivo, "Arquivo não encontrado no classpath: " + nome);
            return new String(arquivo.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void raizDeveEntregarAPaginaInicial() throws IOException {
        prepararGet("/");

        handler.handle(troca);

        verify(troca).sendResponseHeaders(eq(200), anyLong());
        assertAll(
                () -> assertEquals("text/html; charset=utf-8", cabecalhos.getFirst("Content-Type")),
                () -> assertTrue(resposta().startsWith("<!DOCTYPE html>")),
                () -> assertTrue(resposta().contains("<html lang=\"pt-BR\">")));
    }

    @ParameterizedTest
    @CsvSource({
        "/index.html, text/html; charset=utf-8",
        "/style.css, text/css; charset=utf-8",
        "/app.js, text/javascript; charset=utf-8" })
    void arquivosDaListaDevemSerEntreguesComOTipoCorreto(String caminho, String tipo) throws IOException {
        prepararGet(caminho);

        handler.handle(troca);

        verify(troca).sendResponseHeaders(eq(200), anyLong());
        assertEquals(tipo, cabecalhos.getFirst("Content-Type"));
        assertEquals("nosniff", cabecalhos.getFirst("X-Content-Type-Options"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/pom.xml", "/../pom.xml", "/web/index.html", "/index.html/", "/APP.JS",
        "/br/unisul/lanchonete/App.class", "/favicon.ico" })
    void arquivoForaDaListaDeveRetornar404(String caminho) throws IOException {
        prepararGet(caminho);

        handler.handle(troca);

        verify(troca).sendResponseHeaders(eq(404), anyLong());
        assertEquals("Página não encontrada.", resposta());
    }

    @Test
    void arquivoDaListaAusenteNoClasspathDeveRetornar404() throws IOException {
        prepararGet("/");

        new ArquivoEstaticoHandler("/pasta-inexistente/").handle(troca);

        verify(troca).sendResponseHeaders(eq(404), anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT", "DELETE" })
    void metodoDiferenteDeGetDeveRetornar405(String metodo) throws IOException {
        prepararRequisicao(metodo);

        handler.handle(troca);

        verify(troca).sendResponseHeaders(eq(405), anyLong());
        assertEquals("Método não permitido.", resposta());
    }

    @Test
    void nomeDoArquivoDeveTratarARaizComoPaginaInicial() {
        assertEquals("index.html", ArquivoEstaticoHandler.nomeDoArquivo("/"));
        assertEquals("app.js", ArquivoEstaticoHandler.nomeDoArquivo("/app.js"));
    }

    // ---------- Regras de qualidade dos arquivos da tela ----------

    @Test
    void paginaDeveTerCabecalhoHtmlCompleto() throws IOException {
        String html = lerRecurso("index.html");

        assertAll(
                () -> assertTrue(html.startsWith("<!DOCTYPE html>")),
                () -> assertTrue(html.contains("<html lang=\"pt-BR\">")),
                () -> assertTrue(html.contains("<meta charset=\"UTF-8\">")),
                () -> assertTrue(html.contains("<title>Sistema de Lanchonete</title>")));
    }

    @Test
    void todoCampoDaPaginaDeveTerLabel() throws IOException {
        String html = lerRecurso("index.html");
        Matcher campos = Pattern.compile("<(?:select|input)[^>]*\\sid=\"([^\"]+)\"").matcher(html);

        int quantidade = 0;
        while (campos.find()) {
            quantidade++;
            String id = campos.group(1);
            assertTrue(html.contains("<label for=\"" + id + "\">"), "Campo sem <label for>: " + id);
        }
        assertEquals(4, quantidade);
    }

    @Test
    void scriptNaoDeveUsarInnerHtml() throws IOException {
        String js = lerRecurso("app.js");

        assertAll(
                () -> assertFalse(js.contains("innerHTML")),
                () -> assertFalse(js.contains("outerHTML")),
                () -> assertFalse(js.contains("insertAdjacentHTML")),
                () -> assertTrue(js.contains("textContent")));
    }
}
