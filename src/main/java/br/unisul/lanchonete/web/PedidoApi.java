package br.unisul.lanchonete.web;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.service.ClienteNaoEncontradoException;
import br.unisul.lanchonete.service.ClienteService;
import br.unisul.lanchonete.service.EstoqueInsuficienteException;
import br.unisul.lanchonete.service.PedidoService;
import br.unisul.lanchonete.service.ProdutoNaoEncontradoException;
import br.unisul.lanchonete.service.ProdutoService;

/**
 * Traduz as requisições da tela em chamadas aos serviços e devolve JSON.
 * Não depende do servidor HTTP, por isso pode ser testada diretamente.
 *
 * <p>Rotas:
 * <ul>
 * <li>GET /api/clientes, GET /api/produtos, GET /api/pedidos</li>
 * <li>POST /api/pedidos (campo clienteId)</li>
 * <li>POST /api/pedidos/{id}/itens (campos produtoId e quantidade)</li>
 * <li>POST /api/pedidos/{id}/confirmar e POST /api/pedidos/{id}/cancelar</li>
 * </ul>
 *
 * <p>Erros: 400 para dados inválidos ou pedido inexistente, 404 para cliente/produto
 * ou rota inexistente, 405 para método não suportado e 409 para regra de status
 * ou falta de estoque.
 */
public class PedidoApi {

    /**
     * Resposta da API: o código HTTP e o corpo em JSON.
     *
     * @param status código HTTP (ex.: 200, 400, 409)
     * @param corpo  conteúdo JSON da resposta
     */
    public record Resposta(int status, String corpo) {
    }

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PEDIDOS = "/api/pedidos";

    // Ex.: /api/pedidos/3/confirmar -> grupo 1 = "3", grupo 2 = "confirmar"
    private static final Pattern ACAO_DO_PEDIDO = Pattern.compile("^/api/pedidos/(\\d{1,9})/(itens|confirmar|cancelar)$");

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    public PedidoApi(PedidoService pedidoService, ClienteService clienteService, ProdutoService produtoService) {
        this.pedidoService = Objects.requireNonNull(pedidoService, "O serviço de pedidos é obrigatório.");
        this.clienteService = Objects.requireNonNull(clienteService, "O serviço de clientes é obrigatório.");
        this.produtoService = Objects.requireNonNull(produtoService, "O serviço de produtos é obrigatório.");
    }

    /**
     * Processa uma requisição e devolve a resposta em JSON.
     *
     * @param metodo  método HTTP (GET ou POST)
     * @param caminho caminho da URL, ex.: /api/pedidos/1/itens
     * @param corpo   corpo no formato de formulário, ex.: produtoId=1&amp;quantidade=2
     * @return status HTTP e corpo JSON
     */
    public Resposta processar(String metodo, String caminho, String corpo) {
        try {
            if (GET.equals(metodo)) {
                return listar(caminho);
            }
            if (POST.equals(metodo)) {
                return executarAcao(caminho, lerFormulario(corpo));
            }
            return erro(405, "Método não permitido: " + metodo);
        } catch (ClienteNaoEncontradoException | ProdutoNaoEncontradoException e) {
            return erro(404, e.getMessage());
        } catch (IllegalArgumentException e) {
            return erro(400, e.getMessage());
        } catch (IllegalStateException | EstoqueInsuficienteException e) {
            return erro(409, e.getMessage());
        }
    }

    private Resposta listar(String caminho) {
        return switch (caminho) {
            case "/api/clientes" -> ok(Json.lista(clienteService.listarTodos().stream().map(Json::cliente).toList()));
            case "/api/produtos" -> ok(Json.lista(produtoService.listarTodos().stream().map(Json::produto).toList()));
            case PEDIDOS -> ok(Json.lista(pedidoService.listarTodos().stream().map(this::pedidoEmJson).toList()));
            default -> rotaNaoEncontrada(caminho);
        };
    }

    private Resposta executarAcao(String caminho, Map<String, String> campos) {
        if (PEDIDOS.equals(caminho)) {
            Pedido criado = pedidoService.criarPedido((long) lerInteiro(campos, "clienteId"));
            return new Resposta(201, pedidoEmJson(criado));
        }

        Matcher rota = ACAO_DO_PEDIDO.matcher(caminho);
        if (!rota.matches()) {
            return rotaNaoEncontrada(caminho);
        }

        Long pedidoId = Long.valueOf(rota.group(1));
        Pedido pedido = switch (rota.group(2)) {
            case "itens" -> pedidoService.adicionarItem(pedidoId,
                    (long) lerInteiro(campos, "produtoId"), lerInteiro(campos, "quantidade"));
            case "confirmar" -> pedidoService.confirmar(pedidoId);
            default -> pedidoService.cancelar(pedidoId);
        };
        return ok(pedidoEmJson(pedido));
    }

    private String pedidoEmJson(Pedido pedido) {
        String nomeCliente = clienteService.buscarPorId(pedido.getClienteId()).getNome();
        return Json.pedido(pedido, nomeCliente);
    }

    /**
     * Lê um corpo "campo1=valor1&amp;campo2=valor2" e devolve os campos num mapa.
     *
     * @throws IllegalArgumentException se o corpo tiver uma codificação inválida
     */
    static Map<String, String> lerFormulario(String corpo) {
        Map<String, String> campos = new HashMap<>();
        if (corpo == null || corpo.isBlank()) {
            return campos;
        }
        try {
            for (String par : corpo.split("&")) {
                String[] chaveValor = par.split("=", 2);
                String valor = chaveValor.length > 1 ? chaveValor[1] : "";
                campos.put(URLDecoder.decode(chaveValor[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(valor, StandardCharsets.UTF_8));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Os dados enviados estão em um formato inválido.", e);
        }
        return campos;
    }

    private static int lerInteiro(Map<String, String> campos, String nome) {
        String valor = campos.get(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo '" + nome + "' é obrigatório.");
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo '" + nome + "' deve ser um número inteiro.", e);
        }
    }

    private static Resposta ok(String json) {
        return new Resposta(200, json);
    }

    private static Resposta rotaNaoEncontrada(String caminho) {
        return erro(404, "Rota não encontrada: " + caminho);
    }

    private static Resposta erro(int status, String mensagem) {
        return new Resposta(status, Json.erro(mensagem));
    }
}
