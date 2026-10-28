# 🍔 Sistema de Gestão de Lanchonetes

Sistema para gerenciar a operação de uma lanchonete: cadastro de produtos, pedidos, controle de estoque, clientes, funcionários e relatórios.

Projeto desenvolvido para a **A3 da unidade curricular Gestão e Qualidade de Software**, UNISUL, sob orientação do professor Jorge Werner.

---

## 📋 Sumário

- [Problema atendido](#-problema-atendido)
- [Atores](#-atores)
- [Requisitos](#-requisitos)
- [Situação atual](#-situação-atual)
- [Fluxo principal e critérios de aceitação](#-fluxo-principal-e-critérios-de-aceitação)
- [Tecnologias](#-tecnologias)
- [Estrutura do repositório](#-estrutura-do-repositório)
- [Modelo de dados](#-modelo-de-dados)
- [Como executar](#-como-executar)
- [Testes e qualidade](#-testes-e-qualidade)
- [Integração contínua](#-integração-contínua)
- [Convenção de commits](#-convenção-de-commits)
- [Fluxo de trabalho com Git](#-fluxo-de-trabalho-com-git)
- [Contribuições](#-contribuições)
- [Equipe](#-equipe)
- [Licença](#-licença)

---

## 🎯 Problema atendido

Lanchonetes de pequeno porte costumam controlar pedidos e estoque de forma manual, o que gera erros de contagem, vendas de produtos sem estoque e falta de visibilidade sobre o faturamento. Este sistema centraliza esses processos e aplica regras de negócio validadas por testes automatizados.

## 👥 Atores

| Ator | Responsabilidade |
|---|---|
| **Atendente** | Registra clientes e pedidos |
| **Gerente** | Gerencia produtos, estoque, funcionários e consulta relatórios |
| **Cliente** | Entidade cadastrada, associada aos pedidos |

## ✅ Requisitos

### Funcionais

| ID | Requisito | Status |
|---|---|---|
| RF01 | Cadastrar, consultar, atualizar e excluir produtos | ⏳ Planejado |
| RF02 | Cadastrar, consultar, atualizar e excluir clientes | ✅ Implementado |
| RF03 | Criar pedidos com um ou mais itens e acompanhar o status | ⏳ Planejado |
| RF04 | Cancelar pedidos | ⏳ Planejado |
| RF05 | Controlar entradas e saídas de estoque | ⏳ Planejado |
| RF06 | Dar baixa automática no estoque ao confirmar um pedido | ⏳ Planejado |
| RF07 | Autenticar funcionários por login e senha | ⏳ Planejado |
| RF08 | Registrar o pagamento do pedido | ⏳ Planejado |
| RF09 | Gerar relatórios de vendas e de estoque | ⏳ Planejado |

### Não funcionais

| ID | Requisito | Meta |
|---|---|---|
| RNF01 | **Usabilidade** | Fluxo de pedido concluído em poucos passos |
| RNF02 | **Segurança** | Senhas armazenadas com hash (BCrypt); validação de entradas |
| RNF03 | **Desempenho** | Consultas comuns respondem em menos de 1 segundo |
| RNF04 | **Qualidade** | Cobertura de testes ≥ 75% e aprovação no Quality Gate |
| RNF05 | **Manutenibilidade** | Código modular, sem duplicações relevantes |

### Regra de negócio validada por testes

> Ao confirmar um pedido, o sistema deve dar baixa no estoque de cada item. Se algum produto não tiver estoque suficiente, o pedido é rejeitado e nenhuma baixa é feita.

## 📌 Situação atual

**Implementado**

- Build com Maven e Java 21 (`pom.xml`)
- Testes unitários com JUnit 5 e Mockito
- Relatório de cobertura com JaCoCo, gerado no `mvn verify`
- CI no GitHub Actions: compila, testa, gera a cobertura e publica o relatório do JaCoCo
- Módulo Clientes: entidade, repositório em memória, validação de CPF, cadastro com validação de campos e bloqueio de CPF duplicado, consulta por id e por CPF e listagem
- Docker: `Dockerfile` multi-stage (build com testes e cobertura; execução só com JRE) e `docker-compose.yml`

**Planejado**

- Análise no SonarCloud e Quality Gate
- Banco de dados e testes de integração com H2
- Interface e camada de entrada (`controller/`), incluindo a execução pela linha de comando
- Atualização e exclusão de clientes
- Módulos de Produtos, Estoque, Pedidos, Pagamento, Funcionários (login) e Relatórios
- Documentação em `docs/` (UML, BPM e plano de testes)

## 🔄 Fluxo principal e critérios de aceitação

**Fluxo:** o atendente faz login → seleciona ou cadastra o cliente → adiciona produtos ao pedido → confirma → o estoque é atualizado → o pagamento é registrado.

**Critérios de aceitação:**

- [ ] Pedido com estoque suficiente é criado e o estoque diminui na quantidade exata.
- [ ] Pedido com estoque insuficiente é rejeitado com mensagem clara e sem alterar o estoque.
- [ ] Pedido cancelado devolve os itens ao estoque.
- [ ] Valor total do pedido é igual à soma de `quantidade × preço unitário` dos itens.
- [ ] Login com credenciais inválidas é negado.

### Regras de cancelamento e reposição de estoque

**Status do pedido**

```
ABERTO     ──confirmar──▶ CONFIRMADO
ABERTO     ──cancelar───▶ CANCELADO
CONFIRMADO ──cancelar───▶ CANCELADO
```

| Status atual | Ação | Novo status | Efeito no estoque |
|---|---|---|---|
| ABERTO | Confirmar (com estoque suficiente) | CONFIRMADO | Baixa em todos os itens |
| ABERTO | Confirmar (com estoque insuficiente) | ABERTO | Nenhum |
| ABERTO | Cancelar | CANCELADO | Nenhum |
| CONFIRMADO | Cancelar | CANCELADO | Quantidades devolvidas ao estoque |
| CANCELADO | Cancelar | — (erro) | Nenhum |

**Confirmação**

- O sistema verifica o estoque de **todos** os itens antes de dar qualquer baixa.
- Se algum item não tiver estoque suficiente, o pedido inteiro é recusado, nenhuma baixa é feita e o pedido continua ABERTO. A mensagem informa qual produto está sem estoque.
- Se todos os itens tiverem estoque, a baixa é feita em todos eles e o status passa para CONFIRMADO.

**Cancelamento**

- Pedido ABERTO cancelado: o estoque não muda, porque nada foi baixado.
- Pedido CONFIRMADO cancelado: as quantidades de todos os itens voltam ao estoque.
- Pedido já CANCELADO não pode ser cancelado de novo; o sistema retorna um erro com mensagem clara.
- Cancelamento após o pagamento (estorno) está fora do escopo atual e será tratado junto com o RF08.

**Reposição de estoque**

- Feita pelo gerente, por meio de uma entrada de estoque.
- A quantidade deve ser maior que zero e o produto deve existir.
- O registro histórico de cada movimentação (entrada/saída) está planejado.

**Regra x teste**

| Regra | Teste previsto |
|---|---|
| Confirmação com estoque suficiente dá baixa em todos os itens | `deveConfirmarPedidoEDarBaixaEmTodosOsItensQuandoHouverEstoque` |
| Confirmação com estoque insuficiente recusa o pedido sem baixa parcial | `deveRecusarPedidoSemBaixaParcialQuandoEstoqueInsuficiente` |
| Pedido recusado continua ABERTO | `deveManterPedidoAbertoQuandoConfirmacaoForRecusada` |
| Mensagem de recusa informa o produto sem estoque | `deveInformarProdutoSemEstoqueNaMensagemDeErro` |
| Cancelar pedido ABERTO não altera o estoque | `naoDeveAlterarEstoqueAoCancelarPedidoAberto` |
| Cancelar pedido CONFIRMADO devolve os itens ao estoque | `deveDevolverItensAoEstoqueAoCancelarPedidoConfirmado` |
| Pedido CANCELADO não pode ser cancelado de novo | `deveLancarErroAoCancelarPedidoJaCancelado` |
| Reposição com quantidade maior que zero aumenta o estoque | `deveReporEstoqueQuandoQuantidadeForPositiva` |
| Reposição com quantidade zero ou negativa é recusada | `deveRecusarReposicaoComQuantidadeMenorOuIgualAZero` |
| Reposição de produto inexistente é recusada | `deveRecusarReposicaoDeProdutoInexistente` |

> As regras acima estão definidas; a implementação está em andamento no módulo de Pedidos.

## 🛠 Tecnologias

| Camada | Tecnologia | Versão | Status |
|---|---|---|---|
| Linguagem | Java | 21 | ✅ Em uso |
| Build | Maven (maven-compiler-plugin / maven-surefire-plugin) | 3.13.0 / 3.5.2 | ✅ Em uso |
| Testes | JUnit Jupiter | 5.11.4 | ✅ Em uso |
| Testes | Mockito (mockito-junit-jupiter) | 5.14.2 | ✅ Em uso |
| Cobertura | JaCoCo | 0.8.12 | ✅ Em uso |
| CI | GitHub Actions (`actions/checkout`, `actions/setup-java` com Temurin 21, `actions/upload-artifact`) | v5 / v5 / v5 | ✅ Em uso |
| Contêiner | Docker (multi-stage: `maven:3.9-eclipse-temurin-21` no build e `eclipse-temurin:21-jre` na execução) | — | ✅ Em uso |
| Persistência | Repositórios em memória | — | ✅ Em uso |
| Persistência | Banco de dados relacional | — | ⏳ Planejado |
| Testes | H2 (dependência de teste no `pom.xml`, ainda não usada nos testes) | 2.3.232 | ⏳ Planejado |
| Análise estática | SonarCloud | — | ⏳ Planejado |
| Prototipação | Figma (protótipo de interface) | — | ⏳ Planejado |

A stack foi definida pela equipe; a escolha do banco de dados será registrada aqui quando a persistência for implementada.

## 📁 Estrutura do repositório

```
.
├── .github/
│   └── workflows/
│       └── ci.yml            # Pipeline: build, testes e cobertura (Sonar: planejado)
├── docs/                     # planejado
│   ├── uml/                  # planejado: casos de uso, classes e sequência
│   ├── bpm/                  # planejado: fluxo de pedidos e estoque
│   └── plano-de-testes/      # planejado: plano de testes e evidências
├── src/
│   ├── main/java/            # Código-fonte
│   │   ├── model/            # Entidades
│   │   ├── repository/       # Acesso a dados
│   │   ├── service/          # Regras de negócio
│   │   └── controller/       # planejado: camada de entrada
│   ├── main/resources/       # planejado: configurações e scripts SQL
│   └── test/java/            # Testes unitários (integração: planejado)
├── .dockerignore             # Arquivos excluídos do contexto de build da imagem
├── .gitignore
├── docker-compose.yml        # Serviço da aplicação (banco: planejado)
├── Dockerfile                # Imagem multi-stage: build com testes e execução com JRE
├── LICENSE
├── pom.xml
└── README.md
```

## 🗄 Modelo de dados

| Entidade | Atributos principais |
|---|---|
| **Produtos** | id, nome, categoria, preço, estoque |
| **Clientes** | id, nome, cpf, telefone |
| **Funcionários** | id, nome, cargo, login, senha (hash) |
| **Pedidos** | id, id_cliente, valor_total, status |
| **Itens_Pedido** | id, id_pedido, id_produto, quantidade, preço_unitário |
| **Estoque** | id, id_produto, quantidade_entrada, quantidade_saída |

**Relacionamentos:** um cliente tem vários pedidos; um pedido tem vários itens; cada item referencia um produto; cada movimentação de estoque referencia um produto.

## 🚀 Como executar

### Pré-requisitos

- JDK 21+
- Maven 3.9 ou superior
- Git

### Passo a passo

```bash
# 1. Clonar o repositório
git clone https://github.com/LucasdaCosta04/System-Lanchonete.git
cd System-Lanchonete

# 2. Compilar, baixar dependências e rodar os testes
mvn clean install
```

> **Observação:** a execução do fluxo de pedidos pela linha de comando está planejada e ainda não está disponível.

### Com Docker (sem instalar Java ou Maven)

**Pré-requisito:** Docker Desktop ou Docker Engine com o plugin Compose.

```bash
# Construir a imagem e executar a aplicação
docker compose up --build

# Alternativa sem Compose
docker build -t sistema-lanchonete .
docker run --rm sistema-lanchonete
```

O build da imagem executa `mvn verify`, que roda todos os testes e a verificação de cobertura mínima do JaCoCo. Se algum teste falhar ou a cobertura ficar abaixo do mínimo, o build é interrompido e a imagem não é gerada.

### Variáveis de ambiente

O arquivo `.env.example` já existe no repositório, mas essas variáveis só serão usadas quando o projeto tiver banco de dados. Hoje a persistência é em memória, então não é preciso configurá-las para compilar ou testar.

Quando houver banco, copie `.env.example` para `.env` e ajuste os valores. **Nunca** versione credenciais.

| Variável | Descrição |
|---|---|
| `DB_URL` | URL de conexão com o banco |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |

## 🧪 Testes e qualidade

```bash
# Executar todos os testes
mvn test

# Executar testes e gerar relatório de cobertura
mvn verify
# Relatório em: target/site/jacoco/index.html
```

| Tipo | Escopo |
|---|---|
| **Unitários** | Regras de negócio e validações de cada serviço |
| **Integração** | Serviços + repositórios com banco H2 |
| **Sistema** | Fluxo completo de pedido |
| **Usabilidade** | Roteiro de avaliação com o protótipo |

**Metas:** cobertura ≥ 75% · Quality Gate aprovado · zero issues abertos.

### Análise estática e Quality Gate (planejado)

**Ferramenta:** SonarCloud, integrado ao Maven e ao GitHub Actions.

**Passos de integração**

1. *(planejado)* Criar a organização no SonarCloud a partir do GitHub e importar o repositório.
2. *(planejado)* Gerar o token e cadastrá-lo como secret `SONAR_TOKEN` no repositório.
3. *(planejado)* Configurar no `pom.xml` as propriedades `sonar.organization`, `sonar.projectKey` e o caminho do relatório XML do JaCoCo (`sonar.coverage.jacoco.xmlReportPaths`).
4. *(planejado)* Adicionar ao workflow uma etapa que roda `mvn verify sonar:sonar` após os testes.
5. *(planejado)* Desativar a "Automatic Analysis" do SonarCloud para usar a análise via CI.

**Quality Gate:** usar o padrão "Sonar way", que avalia o código novo:

- cobertura mínima de 80%;
- no máximo 3% de linhas duplicadas;
- nota A em confiabilidade, segurança e manutenibilidade;
- 100% dos security hotspots revisados.

**Meta da equipe:** cobertura geral de 80% ou mais, acima dos 75% exigidos na A3.

**Métricas acompanhadas:** segurança, confiabilidade, manutenibilidade, duplicações e complexidade.

**Processo de correção**

- A primeira análise completa gera o registro "antes" (print do painel e lista de issues).
- Cada issue é corrigida em um commit próprio (`fix:` ou `refactor:`), referenciando a regra do Sonar.
- O problema encontrado e a solução aplicada são registrados no relatório da A3 (item 2.12).
- Ao final, é feito um novo registro "depois", com o Quality Gate aprovado.

**Cronograma**

| Etapa | Período |
|---|---|
| Integração do SonarCloud | até 02/11 |
| Primeira análise e registro "antes" | 03/11 a 09/11 |
| Correção dos issues e registro "depois" | 10/11 a 16/11 |

## ⚙️ Integração contínua

A cada `push` e `pull request` para a `main`, o GitHub Actions:

1. Compila o projeto
2. Executa os testes unitários
3. Gera o relatório de cobertura (JaCoCo)
4. Constrói a imagem Docker e executa o contêiner (job `docker`, que só roda após os testes passarem)
5. Envia a análise ao SonarCloud e valida o Quality Gate *(planejado)*

## ✍️ Convenção de commits

Seguimos o padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/):

```
<tipo>(<escopo>): <descrição curta no imperativo>
```

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `test` | Criação ou ajuste de testes |
| `refactor` | Refatoração sem mudar comportamento |
| `docs` | Documentação |
| `ci` | Pipeline e automações |
| `chore` | Manutenção geral |

**Exemplos:**

```
feat(pedido): bloquear pedido com estoque insuficiente
test(produto): adicionar teste de preço negativo
fix(cliente): validar CPF com dígitos repetidos
```

**Regras da equipe:**

- Commits pequenos, atômicos e com finalidade única.
- Cada integrante commita **com a própria conta do GitHub**.
- Cada integrante commita apenas o que ele mesmo escreveu.

## 🌿 Fluxo de trabalho com Git

- Branch principal: `main`
- Trabalho em branches curtas: `feat/nome-da-feature`, `fix/nome-do-bug`
- Integração via Pull Request com pelo menos uma revisão
- Issues abertas pelo professor são prioridade e devem ser resolvidas antes da entrega

## 📊 Contribuições

| Aluno | Funcionalidade | Teste(s) unitário(s) |
|---|---|---|
| Carlos Henrique Lohn | Classe principal da aplicação | [`AppTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/AppTest.java) |
| Carlos Henrique Lohn | Entidade Cliente | Sem teste dedicado; usada em [`ClienteRepositoryEmMemoriaTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/repository/ClienteRepositoryEmMemoriaTest.java) e [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Carlos Henrique Lohn | Validação de CPF | [`CpfValidatorTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/CpfValidatorTest.java) |
| Carlos Henrique Lohn | Repositório de clientes em memória | [`ClienteRepositoryEmMemoriaTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/repository/ClienteRepositoryEmMemoriaTest.java) |
| Carlos Henrique Lohn | Cadastro de cliente com validações e bloqueio de CPF duplicado | [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Carlos Henrique Lohn | Consulta de cliente por id e por CPF e listagem | [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Lucas da Costa | Pedidos | Em desenvolvimento |
| Murilo dos Santos Ferreira | Funcionários e Login | Em desenvolvimento |

### Commits por integrante

| Aluno | Commits de código-fonte | Principais commits |
|---|---|---|
| Carlos Henrique Lohn | 13 | [`97ff666`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/97ff66691358fd1dd6735c014b0fcfa4d9aa4c34) entidade Cliente · [`c06a33a`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/c06a33a41d125cbb38c5c1677f2b1d10950bfdb3) validação de CPF · [`f75f94b`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/f75f94b565a474e58e55ec2e69e82891a5bcb158) repositório em memória · [`e3aaf31`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/e3aaf314eafd422a6c9bf9dd6d4adbb8fb76b664) cadastro com validações · [`11b1414`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/11b14142cdb581c42c16bd38b8992cb94ef3d6f3) bloqueio de CPF duplicado · [`dc7b364`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/dc7b3644668b51f80e6fffd0b183091952c645f1) consultas de cliente |
| Lucas da Costa | Em desenvolvimento | Em desenvolvimento |
| Murilo dos Santos Ferreira | Em desenvolvimento | Em desenvolvimento |

> As tabelas são atualizadas a cada entrega. Só são considerados commits que alteram código-fonte (`src/`) na branch `main`.

## 👨‍💻 Equipe

| Nome | Papel | Módulo | GitHub |
|---|---|---|---|
| _Lucas da Costa_ | Líder de equipe | Pedidos | [@LucasdaCosta04](https://github.com/LucasdaCosta04) |
| _Carlos Henrique Lohn_ | Desenvolvedor | Clientes | [@carloshlohn](https://github.com/carloshlohn) |
| _Murilo dos Santos Ferreira_ | Desenvolvedor | Funcionários e Login | [@MuriloS-Ferreira](https://github.com/MuriloS-Ferreira) |

**Curso:** _Ciência da Computação_ · **Semestre:** _6_ · **Professor:** Jorge Werner

## 📄 Licença

Distribuído sob a licença MIT. Veja o arquivo [`LICENSE`](LICENSE) para mais detalhes.
