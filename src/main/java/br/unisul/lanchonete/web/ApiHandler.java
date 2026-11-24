package br.unisul.lanchonete.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Atende as rotas /api/: lê a requisição, repassa para a {@link PedidoApi} e devolve o JSON.
 */
public class ApiHandler implements HttpHandler {

    static final String TIPO_JSON = "application/json; charset=utf-8";

    // Os formulários da tela são pequenos; corpos maiores são cortados neste limite.
    static final int LIMITE_CORPO = 10_000;

    private final PedidoApi api;

    public ApiHandler(PedidoApi api) {
        this.api = Objects.requireNonNull(api, "A API de pedidos é obrigatória.");
    }

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String corpo = lerCorpo(troca);
        PedidoApi.Resposta resposta = api.processar(troca.getRequestMethod(), troca.getRequestURI().getPath(), corpo);
        ServidorWeb.enviar(troca, resposta.status(), TIPO_JSON, resposta.corpo().getBytes(StandardCharsets.UTF_8));
    }

    private static String lerCorpo(HttpExchange troca) throws IOException {
        try (InputStream entrada = troca.getRequestBody()) {
            return new String(entrada.readNBytes(LIMITE_CORPO), StandardCharsets.UTF_8);
        }
    }
}
