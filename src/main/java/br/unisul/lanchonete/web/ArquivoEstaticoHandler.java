package br.unisul.lanchonete.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Entrega os arquivos da tela (HTML, CSS e JS) guardados em src/main/resources/web/.
 * Só os arquivos da lista fixa podem ser lidos: o caminho da URL nunca vira caminho de arquivo.
 */
public class ArquivoEstaticoHandler implements HttpHandler {

    static final String PASTA_PADRAO = "/web/";
    static final String PAGINA_INICIAL = "index.html";

    private static final String TIPO_TEXTO = "text/plain; charset=utf-8";
    private static final byte[] NAO_ENCONTRADO = "Página não encontrada.".getBytes(StandardCharsets.UTF_8);
    private static final byte[] METODO_NAO_PERMITIDO = "Método não permitido.".getBytes(StandardCharsets.UTF_8);

    // Lista fixa (allowlist) de arquivos servidos e o tipo de cada um.
    private static final Map<String, String> ARQUIVOS = Map.of(
            PAGINA_INICIAL, "text/html; charset=utf-8",
            "style.css", "text/css; charset=utf-8",
            "app.js", "text/javascript; charset=utf-8");

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

        String nome = nomeDoArquivo(troca.getRequestURI().getPath());
        String tipo = ARQUIVOS.get(nome);
        byte[] conteudo = tipo == null ? null : ler(nome);
        if (conteudo == null) {
            ServidorWeb.enviar(troca, 404, TIPO_TEXTO, NAO_ENCONTRADO);
            return;
        }
        ServidorWeb.enviar(troca, 200, tipo, conteudo);
    }

    // "/" vira a página inicial; "/app.js" vira "app.js".
    static String nomeDoArquivo(String caminho) {
        return "/".equals(caminho) ? PAGINA_INICIAL : caminho.substring(1);
    }

    // Lê o arquivo do classpath, ou devolve null se ele não existir.
    private byte[] ler(String nome) throws IOException {
        try (InputStream arquivo = ArquivoEstaticoHandler.class.getResourceAsStream(pasta + nome)) {
            return arquivo == null ? null : arquivo.readAllBytes();
        }
    }
}
