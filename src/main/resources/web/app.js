// Tela do Sistema de Lanchonete. Conversa com a API em /api/ e monta a página
// somente com document.createElement e textContent: nenhum dado vira HTML.

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

const estado = { clientes: [], produtos: [], pedidos: [], selecionadoId: null };

function porId(id) {
  return document.getElementById(id);
}

const tela = {
  alerta: porId('alerta'),
  abas: document.querySelectorAll('[role="tab"]'),
  resumoClientes: porId('resumo-clientes'),
  resumoProdutos: porId('resumo-produtos'),
  resumoPedidos: porId('resumo-pedidos'),
  resumoEstoque: porId('resumo-estoque'),
  ultimosPedidos: porId('ultimos-pedidos'),
  semPedidosInicio: porId('sem-pedidos-inicio'),
  formPedido: porId('form-pedido'),
  formItem: porId('form-item'),
  cliente: porId('cliente'),
  pedidoAberto: porId('pedido-aberto'),
  produto: porId('produto'),
  quantidade: porId('quantidade'),
  tituloDetalhe: porId('titulo-detalhe'),
  acoesDetalhe: porId('acoes-detalhe'),
  detalheVazio: porId('detalhe-vazio'),
  tabelaDetalhe: porId('tabela-detalhe'),
  itensDetalhe: porId('itens-detalhe'),
  totalDetalhe: porId('total-detalhe'),
  listaPedidos: porId('lista-pedidos'),
  semPedidosLista: porId('sem-pedidos-lista'),
  listaClientes: porId('lista-clientes'),
  listaProdutos: porId('lista-produtos'),
};

// ---------- Comunicação com a API ----------

// Envia os campos como formulário e devolve o JSON; em erro, lança a mensagem da API.
async function chamarApi(metodo, caminho, campos) {
  const opcoes = { method: metodo };
  if (campos) {
    opcoes.body = new URLSearchParams(campos);
  }
  const resposta = await fetch(caminho, opcoes);
  const dados = await resposta.json();
  if (!resposta.ok) {
    throw new Error(dados.erro);
  }
  return dados;
}

// ---------- Criação de elementos ----------

function elemento(tag, classe) {
  const novo = document.createElement(tag);
  if (classe) {
    novo.className = classe;
  }
  return novo;
}

function comTexto(tag, conteudo, classe) {
  const novo = elemento(tag, classe);
  novo.textContent = conteudo;
  return novo;
}

function celulaCom(...filhos) {
  const celula = elemento('td');
  celula.append(...filhos);
  return celula;
}

function linha(...celulas) {
  const novaLinha = elemento('tr');
  novaLinha.append(...celulas);
  return novaLinha;
}

function badge(status) {
  return comTexto('span', status, `badge badge-${status.toLowerCase()}`);
}

function botao(texto, classe, aoClicar) {
  const novo = comTexto('button', texto, `botao ${classe}`);
  novo.type = 'button';
  novo.addEventListener('click', aoClicar);
  return novo;
}

function mostrarAlerta(texto, tipo) {
  tela.alerta.textContent = texto;
  tela.alerta.className = `alerta alerta-${tipo}`;
  tela.alerta.hidden = false;
}

function descreverItens(itens) {
  return itens.map((item) => `${item.quantidade} x ${item.nomeProduto}`).join(', ');
}

// ---------- Desenho de cada parte da tela ----------

function acoesDoPedido(pedido, mostrarVer) {
  const acoes = elemento('div', 'acoes');
  if (mostrarVer) {
    acoes.append(botao('Ver', 'botao-ver', () => selecionarPedido(pedido.id)));
  }
  if (pedido.status === 'ABERTO') {
    acoes.append(botao('Confirmar', 'botao-confirmar', () => confirmar(pedido)));
  }
  if (pedido.status !== 'CANCELADO') {
    acoes.append(botao('Cancelar', 'botao-cancelar', () => cancelar(pedido)));
  }
  return acoes;
}

function renderizarResumo() {
  const totalEmEstoque = estado.produtos.reduce((soma, produto) => soma + produto.estoque, 0);
  tela.resumoClientes.textContent = String(estado.clientes.length);
  tela.resumoProdutos.textContent = String(estado.produtos.length);
  tela.resumoPedidos.textContent = String(estado.pedidos.length);
  tela.resumoEstoque.textContent = String(totalEmEstoque);
}

function renderizarUltimosPedidos() {
  const ultimos = estado.pedidos.slice(-5).toReversed();
  tela.ultimosPedidos.replaceChildren(...ultimos.map((pedido) => linha(
    comTexto('td', `#${pedido.id}`),
    comTexto('td', pedido.cliente),
    celulaCom(badge(pedido.status)),
    comTexto('td', moeda.format(pedido.total), 'numero'))));
  tela.semPedidosInicio.hidden = ultimos.length > 0;
}

function renderizarPedidos() {
  tela.listaPedidos.replaceChildren(...estado.pedidos.map((pedido) => linha(
    comTexto('td', `#${pedido.id}`),
    comTexto('td', pedido.cliente),
    celulaCom(badge(pedido.status)),
    comTexto('td', descreverItens(pedido.itens) || 'Sem itens'),
    comTexto('td', moeda.format(pedido.total), 'numero'),
    celulaCom(acoesDoPedido(pedido, true)))));
  tela.semPedidosLista.hidden = estado.pedidos.length > 0;
}

function pedidoSelecionado() {
  return estado.pedidos.find((pedido) => pedido.id === estado.selecionadoId);
}

function linhasDosItens(itens) {
  if (itens.length === 0) {
    const aviso = comTexto('td', 'Nenhum item adicionado ainda.', 'vazio');
    aviso.colSpan = 4;
    return [linha(aviso)];
  }
  return itens.map((item) => linha(
    comTexto('td', item.nomeProduto),
    comTexto('td', String(item.quantidade), 'numero'),
    comTexto('td', moeda.format(item.precoUnitario), 'numero'),
    comTexto('td', moeda.format(item.subtotal), 'numero')));
}

function renderizarDetalhe() {
  const pedido = pedidoSelecionado();
  tela.detalheVazio.hidden = Boolean(pedido);
  tela.tabelaDetalhe.hidden = !pedido;
  if (!pedido) {
    tela.tituloDetalhe.textContent = 'Detalhe do pedido';
    tela.acoesDetalhe.replaceChildren();
    return;
  }
  tela.tituloDetalhe.replaceChildren(
    document.createTextNode(`Pedido #${pedido.id} - ${pedido.cliente} `), badge(pedido.status));
  tela.acoesDetalhe.replaceChildren(acoesDoPedido(pedido, false));
  tela.itensDetalhe.replaceChildren(...linhasDosItens(pedido.itens));
  tela.totalDetalhe.textContent = moeda.format(pedido.total);
}

// Recria as opções do select, mantendo a escolha anterior (ou a preferida) se ainda existir.
function preencherSelect(select, itens, rotulo, preferido) {
  const desejado = String(preferido ?? select.value);
  select.replaceChildren(...itens.map((item) => {
    const opcao = comTexto('option', rotulo(item));
    opcao.value = String(item.id);
    return opcao;
  }));
  if (itens.some((item) => String(item.id) === desejado)) {
    select.value = desejado;
  }
}

function renderizarFormularios() {
  const abertos = estado.pedidos.filter((pedido) => pedido.status === 'ABERTO');
  preencherSelect(tela.cliente, estado.clientes, (cliente) => cliente.nome);
  preencherSelect(tela.produto, estado.produtos,
    (produto) => `${produto.nome} - ${moeda.format(produto.preco)} (saldo: ${produto.estoque})`);
  preencherSelect(tela.pedidoAberto, abertos,
    (pedido) => `Pedido #${pedido.id} - ${pedido.cliente}`, estado.selecionadoId);
}

function renderizarClientes() {
  tela.listaClientes.replaceChildren(...estado.clientes.map((cliente) => linha(
    comTexto('td', `#${cliente.id}`),
    comTexto('td', cliente.nome),
    comTexto('td', cliente.telefone ?? '-'))));
}

function renderizarProdutos() {
  tela.listaProdutos.replaceChildren(...estado.produtos.map((produto) => linha(
    comTexto('td', produto.nome),
    comTexto('td', produto.categoria),
    comTexto('td', moeda.format(produto.preco), 'numero'),
    comTexto('td', String(produto.estoque), produto.estoque === 0 ? 'numero saldo-zerado' : 'numero'))));
}

function renderizarTudo() {
  if (!pedidoSelecionado()) {
    estado.selecionadoId = null;
  }
  renderizarFormularios();
  if (estado.selecionadoId === null && tela.pedidoAberto.value) {
    estado.selecionadoId = Number(tela.pedidoAberto.value);
  }
  renderizarResumo();
  renderizarUltimosPedidos();
  renderizarPedidos();
  renderizarDetalhe();
  renderizarClientes();
  renderizarProdutos();
}

async function atualizarDados() {
  const [clientes, produtos, pedidos] = await Promise.all([
    chamarApi('GET', '/api/clientes'),
    chamarApi('GET', '/api/produtos'),
    chamarApi('GET', '/api/pedidos'),
  ]);
  Object.assign(estado, { clientes, produtos, pedidos });
  renderizarTudo();
}

// ---------- Ações ----------

// Executa a ação na API, recarrega os dados (estoque incluso) e mostra o resultado.
function executar(acao, mensagemDeSucesso, mensagemDeErro = (erro) => erro.message) {
  acao()
    .then(async (resultado) => {
      const mensagem = mensagemDeSucesso(resultado);
      await atualizarDados();
      mostrarAlerta(mensagem, 'sucesso');
    })
    .catch((erro) => mostrarAlerta(mensagemDeErro(erro), 'erro'));
}

function selecionarPedido(id) {
  estado.selecionadoId = id;
  tela.pedidoAberto.value = String(id);
  renderizarDetalhe();
}

function confirmar(pedido) {
  estado.selecionadoId = pedido.id;
  executar(
    () => chamarApi('POST', `/api/pedidos/${pedido.id}/confirmar`),
    (atualizado) => `Pedido #${atualizado.id} confirmado. Baixa no estoque: ${descreverItens(atualizado.itens)}.`,
    (erro) => `Confirmação recusada: ${erro.message} Nenhuma baixa foi feita e o pedido continua ABERTO.`);
}

function mensagemDeCancelamento(statusAnterior, atualizado) {
  if (statusAnterior === 'CONFIRMADO') {
    return `Pedido #${atualizado.id} cancelado. Estoque reposto: ${descreverItens(atualizado.itens)}.`;
  }
  return `Pedido #${atualizado.id} cancelado. Como estava ABERTO, o estoque não foi alterado.`;
}

function cancelar(pedido) {
  estado.selecionadoId = pedido.id;
  executar(
    () => chamarApi('POST', `/api/pedidos/${pedido.id}/cancelar`),
    (atualizado) => mensagemDeCancelamento(pedido.status, atualizado));
}

function ativarAba(abaEscolhida) {
  for (const aba of tela.abas) {
    const ativa = aba === abaEscolhida;
    aba.setAttribute('aria-selected', String(ativa));
    aba.classList.toggle('ativa', ativa);
    porId(aba.getAttribute('aria-controls')).hidden = !ativa;
  }
}

// ---------- Eventos ----------

for (const aba of tela.abas) {
  aba.addEventListener('click', () => ativarAba(aba));
}

tela.pedidoAberto.addEventListener('change', () => selecionarPedido(Number(tela.pedidoAberto.value)));

tela.formPedido.addEventListener('submit', (evento) => {
  evento.preventDefault();
  executar(
    () => chamarApi('POST', '/api/pedidos', { clienteId: tela.cliente.value }),
    (pedido) => {
      estado.selecionadoId = pedido.id;
      return `Pedido #${pedido.id} criado para ${pedido.cliente}. Agora adicione os itens.`;
    });
});

tela.formItem.addEventListener('submit', (evento) => {
  evento.preventDefault();
  const pedidoId = tela.pedidoAberto.value;
  if (pedidoId === '') {
    mostrarAlerta('Crie um pedido antes de adicionar itens.', 'erro');
    return;
  }
  estado.selecionadoId = Number(pedidoId);
  executar(
    () => chamarApi('POST', `/api/pedidos/${pedidoId}/itens`, {
      produtoId: tela.produto.value,
      quantidade: tela.quantidade.value,
    }),
    (pedido) => `Item adicionado ao pedido #${pedido.id}. Total atual: ${moeda.format(pedido.total)}.`);
});

// Carga inicial: a tela já abre com o cenário base (Maria Silva e os três produtos).
function iniciar() {
  atualizarDados()
    .catch((erro) => mostrarAlerta(`Não foi possível carregar os dados: ${erro.message}`, 'erro'));
}

iniciar();
