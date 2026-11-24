package br.unisul.lanchonete.web;

import java.util.List;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.model.ItemPedido;
import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.Produto;

/**
 * Monta os textos JSON enviados para a tela, sem bibliotecas externas.
 */
final class Json {

    private static final String ABRE_COM_ID = "{\"id\":";

    private Json() {
    }

    /**
     * Converte um texto em string JSON (entre aspas), escapando os caracteres especiais.
     * Os sinais de menor e maior também são escapados, por segurança.
     */
    static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        StringBuilder json = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            json.append(escapar(c));
        }
        return json.append('"').toString();
    }

    private static String escapar(char c) {
        return switch (c) {
            case '"' -> "\\\"";
            case '\\' -> "\\\\";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            case '<', '>' -> unicode(c);
            default -> c < 0x20 ? unicode(c) : String.valueOf(c);
        };
    }

    private static String unicode(char c) {
        return String.format("\\u%04x", (int) c);
    }

    static String lista(List<String> objetos) {
        return "[" + String.join(",", objetos) + "]";
    }

    static String erro(String mensagem) {
        return "{\"erro\":" + texto(mensagem) + "}";
    }

    // O CPF não é enviado para a tela.
    static String cliente(Cliente cliente) {
        return ABRE_COM_ID + cliente.getId()
                + ",\"nome\":" + texto(cliente.getNome())
                + ",\"telefone\":" + texto(cliente.getTelefone()) + "}";
    }

    static String produto(Produto produto) {
        return ABRE_COM_ID + produto.getId()
                + ",\"nome\":" + texto(produto.getNome())
                + ",\"categoria\":" + texto(produto.getCategoria())
                + ",\"preco\":" + produto.getPreco().toPlainString()
                + ",\"estoque\":" + produto.getQuantidadeEstoque() + "}";
    }

    static String pedido(Pedido pedido, String nomeCliente) {
        List<String> itens = pedido.getItens().stream().map(Json::item).toList();
        return ABRE_COM_ID + pedido.getId()
                + ",\"cliente\":" + texto(nomeCliente)
                + ",\"status\":" + texto(pedido.getStatus().name())
                + ",\"total\":" + pedido.getTotal().toPlainString()
                + ",\"itens\":" + lista(itens) + "}";
    }

    private static String item(ItemPedido item) {
        return "{\"produtoId\":" + item.getProdutoId()
                + ",\"nomeProduto\":" + texto(item.getNomeProduto())
                + ",\"quantidade\":" + item.getQuantidade()
                + ",\"precoUnitario\":" + item.getPrecoUnitario().toPlainString()
                + ",\"subtotal\":" + item.getSubtotal().toPlainString() + "}";
    }
}
