package br.unisul.lanchonete.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Servidor HTTP local (nativo do Java) que entrega a tela e a API de pedidos.
 * Escuta somente em 127.0.0.1 e atende uma requisição por vez, o que mantém
 * seguros os repositórios em memória.
 */
public class ServidorWeb {

    public static final String HOST = "127.0.0.1";
    private static final String ENDERECO_BASE = "http://127.0.0.1:";

    private final HttpServer servidor;

    /**
     * Prepara o servidor na porta informada (0 escolhe uma porta livre).
     *
     * @param api   API que atende as rotas /api/
     * @param porta porta local
     * @throws UncheckedIOException se a porta não puder ser aberta
     */
    public ServidorWeb(PedidoApi api, int porta) {
        ApiHandler apiHandler = new ApiHandler(api);
        try {
            this.servidor = HttpServer.create(new InetSocketAddress(HOST, porta), 0);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível abrir o servidor na porta " + porta + ".", e);
        }
        servidor.createContext("/api/", apiHandler);
        servidor.createContext("/", new ArquivoEstaticoHandler());
    }

    public void iniciar() {
        servidor.start();
    }

    public void parar() {
        servidor.stop(0);
    }

    public int getPorta() {
        return servidor.getAddress().getPort();
    }

    // Endereço para abrir no navegador, ex.: http://127.0.0.1:8080
    public String getEndereco() {
        return ENDERECO_BASE + getPorta();
    }

    // Escreve a resposta HTTP (cabeçalhos e corpo) e fecha a troca.
    static void enviar(HttpExchange troca, int status, String tipo, byte[] conteudo) throws IOException {
        troca.getResponseHeaders().set("Content-Type", tipo);
        troca.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        troca.sendResponseHeaders(status, conteudo.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(conteudo);
        }
    }
}
