package br.unisul.lanchonete.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import br.unisul.lanchonete.web.PedidoApi.Resposta;

@ExtendWith(MockitoExtension.class)
class ApiHandlerTest {

    @Mock
    private PedidoApi api;

    @Mock
    private HttpExchange troca;

    private final Headers cabecalhos = new Headers();
    private final ByteArrayOutputStream corpoDaResposta = new ByteArrayOutputStream();

    private ApiHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiHandler(api);
    }

    private void prepararRequisicao(String metodo, String caminho, String corpo) {
        when(troca.getRequestMethod()).thenReturn(metodo);
        when(troca.getRequestURI()).thenReturn(URI.create(caminho));
        when(troca.getRequestBody()).thenReturn(new ByteArrayInputStream(corpo.getBytes(StandardCharsets.UTF_8)));
        when(troca.getResponseHeaders()).thenReturn(cabecalhos);
        when(troca.getResponseBody()).thenReturn(corpoDaResposta);
    }

    @Test
    void construtorDeveExigirApi() {
        assertThrows(NullPointerException.class, () -> new ApiHandler(null));
    }

    @Test
    void deveRepassarRequisicaoParaApiEEscreverRespostaJson() throws IOException {
        prepararRequisicao("POST", "/api/pedidos", "clienteId=1");
        String json = "{\"id\":1,\"cliente\":\"Maria Silva\"}";
        when(api.processar("POST", "/api/pedidos", "clienteId=1")).thenReturn(new Resposta(201, json));

        handler.handle(troca);

        byte[] esperado = json.getBytes(StandardCharsets.UTF_8);
        verify(troca).sendResponseHeaders(201, esperado.length);
        assertEquals(json, corpoDaResposta.toString(StandardCharsets.UTF_8));
        assertEquals(ApiHandler.TIPO_JSON, cabecalhos.getFirst("Content-Type"));
        assertEquals("nosniff", cabecalhos.getFirst("X-Content-Type-Options"));
    }

    @Test
    void deveUsarSomenteOCaminhoDaUrlSemQueryString() throws IOException {
        prepararRequisicao("GET", "/api/produtos?x=1", "");
        when(api.processar("GET", "/api/produtos", "")).thenReturn(new Resposta(200, "[]"));

        handler.handle(troca);

        verify(troca).sendResponseHeaders(200, 2);
    }

    @Test
    void corpoMuitoGrandeDeveSerCortadoNoLimite() throws IOException {
        prepararRequisicao("POST", "/api/pedidos", "a".repeat(ApiHandler.LIMITE_CORPO + 500));
        when(api.processar(eq("POST"), eq("/api/pedidos"), anyString())).thenReturn(new Resposta(400, "{}"));

        handler.handle(troca);

        ArgumentCaptor<String> corpoLido = ArgumentCaptor.forClass(String.class);
        verify(api).processar(eq("POST"), eq("/api/pedidos"), corpoLido.capture());
        assertEquals(ApiHandler.LIMITE_CORPO, corpoLido.getValue().length());
    }
}
