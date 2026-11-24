package br.unisul.lanchonete.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Entrega os arquivos da tela (HTML, CSS e JS) guardados em src/main/resources/web/.
 * Só os arquivos da lista fixa podem ser lidos: o caminho da URL nunca vira caminho de arquivo,
 * porque o switch devolve sempre um nome literal escrito aqui no código.
 */
public class ArquivoEstaticoHandler implements HttpHandler {

    static final String PASTA_PADRAO = "/web/";
    static final String PAGINA_INICIAL = "index.html";

    private static final String TIPO_TEXTO = "text/plain; charset=utf-8";
    private static final byte[] NAO_ENCONTRADO = "Página não encontrada.".getBytes(StandardCharsets.UTF_8);
    private static final byte[] METODO_NAO_PERMITIDO = "Método não permitido.".getBytes(StandardCharsets.UTF_8);

    // Um arquivo da lista fixa: nome dentro da pasta e Content-Type.
    record Arquivo(String nome, String tipo) {
    }

    private final String pasta;

    public ArquivoEstaticoHandler() {
        this(PASTA_PADRAO);
    }

    // Permite apontar para outra pasta do classpath (usado nos testes).
    ArquivoEstaticoHandler(String pasta) {
        this.pasta = pasta;
    }

    @Override
    public void handle(HttpExchange troca) throws IOException {
        if (!"GET".equals(troca.getRequestMethod())) {
            ServidorWeb.enviar(troca, 405, TIPO_TEXTO, METODO_NAO_PERMITIDO);
            return;
        }

        Arquivo arquivo = arquivoDaRequisicao(troca.getRequestURI().getPath());
        byte[] conteudo = arquivo == null ? null : ler(arquivo.nome());
        if (conteudo == null) {
            ServidorWeb.enviar(troca, 404, TIPO_TEXTO, NAO_ENCONTRADO);
            return;
        }
        ServidorWeb.enviar(troca, 200, arquivo.tipo(), conteudo);
    }

    // Lista fixa (allowlist): cada caminho aceito vira um nome literal; qualquer outro vira null.
    static Arquivo arquivoDaRequisicao(String caminho) {
        return switch (caminho) {
            case "/", "/index.html" -> new Arquivo(PAGINA_INICIAL, "text/html; charset=utf-8");
            case "/style.css" -> new Arquivo("style.css", "text/css; charset=utf-8");
            case "/app.js" -> new Arquivo("app.js", "text/javascript; charset=utf-8");
            default -> null;
        };
    }

    // Lê do classpath um arquivo da lista fixa, ou devolve null se ele não existir.
    private byte[] ler(String nome) throws IOException {
        try (InputStream arquivo = ArquivoEstaticoHandler.class.getResourceAsStream(pasta + nome)) {
            return arquivo == null ? null : arquivo.readAllBytes();
        }
    }
}
