package br.unisul.lanchonete;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;

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
import br.unisul.lanchonete.web.PedidoApi;
import br.unisul.lanchonete.web.ServidorWeb;

/**
 * Ponto de entrada do Sistema de Gestão de Lanchonetes.
 * Executa um roteiro demonstrativo do fluxo de pedidos, sem ler nada do teclado
 * (assim roda também de forma não interativa, como no "docker run" do CI).
 */
public class App {

    public static final String TITULO = "Sistema de Lanchonete";
    static final String ARGUMENTO_WEB = "--web";
    static final String ARGUMENTO_PORTA = "--porta=";
    static final int PORTA_PADRAO = 8080;
    private static final int PORTA_MAXIMA = 65_535;

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

    /**
     * Sem argumentos: executa o roteiro no console (como no "docker run" do CI).
     * Com "--web" (e opcionalmente "--porta=NNNN"): abre a interface web local.
     */
    public static void main(String[] args) {
        App app = new App(System.out);
        if (Arrays.asList(args).contains(ARGUMENTO_WEB)) {
            ServidorWeb servidor = app.iniciarWeb(lerPorta(args));
            app.encerrarAoSair(servidor);
        } else {
            app.executar();
        }
    }

    /**
     * Lê a porta do argumento "--porta=NNNN"; sem ele, usa a porta padrão 8080.
     *
     * @throws IllegalArgumentException se a porta não for um número entre 0 e 65535
     */
    static int lerPorta(String[] args) {
        for (String argumento : args) {
            if (argumento.startsWith(ARGUMENTO_PORTA)) {
                return converterPorta(argumento.substring(ARGUMENTO_PORTA.length()));
            }
        }
        return PORTA_PADRAO;
    }

    private static int converterPorta(String texto) {
        int porta;
        try {
            porta = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(mensagemPortaInvalida(texto), e);
        }
        if (porta < 0 || porta > PORTA_MAXIMA) {
            throw new IllegalArgumentException(mensagemPortaInvalida(texto));
        }
        return porta;
    }

    private static String mensagemPortaInvalida(String texto) {
        return "Porta inválida: '" + texto + "'. Use um número entre 0 e " + PORTA_MAXIMA + ".";
    }

    /**
     * Cadastra o cenário base e sobe a interface web local na porta informada.
     *
     * @param porta porta local (0 escolhe uma porta livre, usado nos testes)
     * @return o servidor iniciado, para que possa ser parado
     */
    ServidorWeb iniciarWeb(int porta) {
        saida.println("=== " + TITULO + " (modo web) ===");
        cadastrarCenarioBase();

        ServidorWeb servidor = new ServidorWeb(new PedidoApi(pedidoService, clienteService, produtoService), porta);
        servidor.iniciar();

        saida.println();
        saida.println("Interface web disponível em " + servidor.getEndereco());
        saida.println("Pressione Ctrl+C para encerrar.");
        return servidor;
    }

    // Ao apertar Ctrl+C, a JVM executa este gancho e o servidor é parado de forma limpa.
    private void encerrarAoSair(ServidorWeb servidor) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servidor.parar();
            saida.println("Servidor web encerrado.");
        }, "encerrar-servidor-web"));
    }

    /**
     * Executa o roteiro: cadastro, pedido confirmado, pedido recusado e cancelamento com reposição.
     */
    void executar() {
        saida.println("=== " + TITULO + " ===");

        // Passo 1: cadastro de cliente e produtos.
        titulo("Passo 1: cadastro de cliente e produtos");
        CenarioBase base = cadastrarCenarioBase();
        Cliente cliente = base.cliente();
        Produto lanche = base.lanche();
        Produto bebida = base.bebida();
        Produto batata = base.batata();

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

    // Cliente e produtos usados tanto no roteiro do console quanto na tela web.
    private record CenarioBase(Cliente cliente, Produto lanche, Produto bebida, Produto batata) {
    }

    private CenarioBase cadastrarCenarioBase() {
        Cliente cliente = clienteService.cadastrar(new Cliente("Maria Silva", "12345678909", "(48) 99999-0000"));
        saida.println("Cliente cadastrado: " + cliente.getNome() + " (id " + cliente.getId() + ")");

        Produto lanche = cadastrarProduto("X-Burguer", "Lanche", "18.00", 10);
        Produto bebida = cadastrarProduto("Refrigerante", "Bebida", "6.00", 5);
        Produto batata = cadastrarProduto("Batata Frita", "Porção", "9.50", 3);
        imprimirEstoque("Estoque inicial");
        return new CenarioBase(cliente, lanche, bebida, batata);
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
