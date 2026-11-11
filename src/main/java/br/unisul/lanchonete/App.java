package br.unisul.lanchonete;

import java.io.PrintStream;
import java.math.BigDecimal;

import br.unisul.lanchonete.model.Cliente;
import br.unisul.lanchonete.model.ItemPedido;
import br.unisul.lanchonete.model.Pedido;
import br.unisul.lanchonete.model.Produto;
import br.unisul.lanchonete.repository.ClienteRepositoryEmMemoria;
import br.unisul.lanchonete.repository.PedidoRepositoryEmMemoria;
import br.unisul.lanchonete.repository.ProdutoRepositoryEmMemoria;
import br.unisul.lanchonete.service.ClienteService;
import br.unisul.lanchonete.service.EstoqueInsuficienteException;
import br.unisul.lanchonete.service.EstoqueService;
import br.unisul.lanchonete.service.PedidoService;
import br.unisul.lanchonete.service.ProdutoService;

/**
 * Ponto de entrada do Sistema de Gestão de Lanchonetes.
 * Executa um roteiro demonstrativo do fluxo de pedidos, sem ler nada do teclado
 * (assim roda também de forma não interativa, como no "docker run" do CI).
 */
public class App {

    public static final String TITULO = "Sistema de Lanchonete";

    private final PrintStream saida;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final PedidoService pedidoService;

    // Recebe onde imprimir (o console no main; um buffer nos testes) e monta o sistema em memória.
    App(PrintStream saida) {
        this.saida = saida;

        ClienteRepositoryEmMemoria clienteRepository = new ClienteRepositoryEmMemoria();
        ProdutoRepositoryEmMemoria produtoRepository = new ProdutoRepositoryEmMemoria();
        PedidoRepositoryEmMemoria pedidoRepository = new PedidoRepositoryEmMemoria();

        EstoqueService estoqueService = new EstoqueService(produtoRepository);
        this.clienteService = new ClienteService(clienteRepository);
        this.produtoService = new ProdutoService(produtoRepository);
        this.pedidoService = new PedidoService(pedidoRepository, clienteRepository, produtoRepository,
                estoqueService);
    }

    public static void main(String[] args) {
        new App(System.out).executar();
    }

    /**
     * Executa o roteiro: cadastro, pedido confirmado, pedido recusado e cancelamento com reposição.
     */
    void executar() {
        saida.println("=== " + TITULO + " ===");

        // Passo 1: cadastro de cliente e produtos.
        titulo("Passo 1: cadastro de cliente e produtos");
        Cliente cliente = clienteService.cadastrar(new Cliente("Maria Silva", "12345678909", "(48) 99999-0000"));
        saida.println("Cliente cadastrado: " + cliente.getNome() + " (id " + cliente.getId() + ")");

        Produto lanche = cadastrarProduto("X-Burguer", "Lanche", "18.00", 10);
        Produto bebida = cadastrarProduto("Refrigerante", "Bebida", "6.00", 5);
        Produto batata = cadastrarProduto("Batata Frita", "Porção", "9.50", 3);
        imprimirEstoque("Estoque inicial");

        // Passo 2: pedido com estoque suficiente, confirmado com baixa automática.
        titulo("Passo 2: Pedido 1 com estoque suficiente");
        Pedido pedido1 = pedidoService.criarPedido(cliente.getId());
        saida.println("Pedido " + pedido1.getId() + " criado para " + cliente.getNome()
                + " (status " + pedido1.getStatus() + ")");
        pedidoService.adicionarItem(pedido1.getId(), lanche.getId(), 2);
        pedidoService.adicionarItem(pedido1.getId(), bebida.getId(), 2);
        pedidoService.adicionarItem(pedido1.getId(), batata.getId(), 1);
        imprimirItens(pedido1);

        pedidoService.confirmar(pedido1.getId());
        saida.println("Pedido " + pedido1.getId() + " confirmado (status " + pedido1.getStatus() + ")");
        saida.println("Total do pedido: " + reais(pedido1.getTotal()));
        imprimirEstoque("Estoque após a baixa automática");

        // Passo 3: pedido com quantidade acima do estoque, recusado sem baixa parcial.
        titulo("Passo 3: Pedido 2 com quantidade acima do estoque");
        Pedido pedido2 = pedidoService.criarPedido(cliente.getId());
        pedidoService.adicionarItem(pedido2.getId(), lanche.getId(), 1);
        pedidoService.adicionarItem(pedido2.getId(), batata.getId(), 5);
        saida.println("Pedido " + pedido2.getId() + " criado (status " + pedido2.getStatus() + ")");
        imprimirItens(pedido2);

        try {
            pedidoService.confirmar(pedido2.getId());
        } catch (EstoqueInsuficienteException e) {
            saida.println("Confirmação recusada: " + e.getMessage());
        }
        saida.println("Pedido " + pedido2.getId() + " segue com status " + pedido2.getStatus());
        imprimirEstoque("Estoque intacto (nenhuma baixa parcial)");

        // Passo 4: cancelamento do pedido confirmado, com reposição automática.
        titulo("Passo 4: cancelamento do Pedido 1 (CONFIRMADO)");
        pedidoService.cancelar(pedido1.getId());
        saida.println("Pedido " + pedido1.getId() + " cancelado (status " + pedido1.getStatus() + ")");
        imprimirEstoque("Estoque após a reposição automática");

        saida.println();
        saida.println("=== Fim da demonstração ===");
    }

    private Produto cadastrarProduto(String nome, String categoria, String preco, int estoque) {
        Produto produto = produtoService.cadastrar(new Produto(nome, categoria, new BigDecimal(preco), estoque));
        saida.println("Produto cadastrado: " + produto.getNome() + " | " + reais(produto.getPreco()));
        return produto;
    }

    private void titulo(String texto) {
        saida.println();
        saida.println("--- " + texto + " ---");
    }

    private void imprimirItens(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            saida.println("  + " + item.getQuantidade() + " x " + item.getNomeProduto()
                    + " (" + reais(item.getPrecoUnitario()) + " cada)");
        }
    }

    private void imprimirEstoque(String texto) {
        saida.println(texto + ":");
        for (Produto produto : produtoService.listarTodos()) {
            saida.println("  " + produto.getNome() + ": " + produto.getQuantidadeEstoque());
        }
    }

    // Formata o valor como "R$ 18,00".
    private static String reais(BigDecimal valor) {
        return "R$ " + valor.toPlainString().replace('.', ',');
    }
}
