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
| RF01 | Cadastrar, consultar, atualizar e excluir produtos | ✅ Implementado |
| RF02 | Cadastrar, consultar, atualizar e excluir clientes | ✅ Implementado |
| RF03 | Criar pedidos com um ou mais itens e acompanhar o status | ✅ Implementado |
| RF04 | Cancelar pedidos | ✅ Implementado |
| RF05 | Controlar entradas e saídas de estoque | ✅ Implementado |
| RF06 | Dar baixa automática no estoque ao confirmar um pedido | ✅ Implementado |
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
- Testes de integração (`ClienteServiceIntegracaoTest`, com `@Tag("integracao")`), usando o repositório em memória real
- Relatório de cobertura com JaCoCo, gerado no `mvn verify`
- Cobertura mínima de 75% de linhas exigida no build (`jacoco:check`)
- CI no GitHub Actions: compila, testa, gera a cobertura e publica o relatório do JaCoCo
- Módulo Clientes: entidade, repositório em memória, validação de CPF, cadastro com validação de campos e bloqueio de CPF duplicado, consulta por id e por CPF, listagem, atualização e exclusão
- Docker: `Dockerfile` multi-stage (build com testes e cobertura; execução só com JRE) e `docker-compose.yml`
- Análise estática e Quality Gate integrados ao SonarCloud via GitHub Actions
- Módulo Produtos: entidade `Produto`, `ProdutoRepository` e `ProdutoRepositoryEmMemoria`, `ProdutoService` (cadastro com validação de nome, categoria, preço e estoque; consulta por id e listagem) e `ProdutoNaoEncontradoException`
- Módulo Estoque: `EstoqueService` com verificação de saldo (`possuiEstoque`), baixa (`darBaixa`, que lança `EstoqueInsuficienteException`) e reposição (`repor`)
- Módulo Pedidos completo: enum `StatusPedido`, `ItemPedido` (com cálculo de subtotal), `Pedido` (com cálculo de total, lista imutável de itens e bloqueio de transições inválidas), `PedidoRepositoryEmMemoria` e `PedidoService` (criação, adição de itens, confirmação com baixa de todos os itens sem baixa parcial e cancelamento com reposição)
- Testes de integração do fluxo de pedidos (`PedidoServiceIntegracaoTest`, com `@Tag("integracao")`), usando os serviços e repositórios em memória reais
- Execução demonstrativa do fluxo completo em `App.main`: cadastro, pedido confirmado com baixa, pedido recusado sem baixa parcial e cancelamento com reposição

**Planejado**

- Banco de dados e testes de integração com H2
- Módulos de Funcionários (login), Pagamento e Relatórios
- Documentação em `docs/` (UML, BPM e plano de testes)

## 🔄 Fluxo principal e critérios de aceitação

**Fluxo:** o atendente faz login → seleciona ou cadastra o cliente → adiciona produtos ao pedido → confirma → o estoque é atualizado → o pagamento é registrado.

**Critérios de aceitação:**

- [x] Pedido com estoque suficiente é criado e o estoque diminui na quantidade exata.
- [x] Pedido com estoque insuficiente é rejeitado com mensagem clara e sem alterar o estoque.
- [x] Pedido cancelado devolve os itens ao estoque.
- [x] Valor total do pedido é igual à soma de `quantidade × preço unitário` dos itens.
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

> Todas as regras acima estão implementadas e validadas por testes unitários e de integração em `EstoqueServiceTest`, `PedidoServiceTest` e `PedidoServiceIntegracaoTest`.

## 🛠 Tecnologias

| Camada | Tecnologia | Versão | Status |
|---|---|---|---|
| Linguagem | Java | 21 | ✅ Em uso |
| Build | Maven (maven-compiler-plugin / maven-surefire-plugin) | 3.13.0 / 3.5.2 | ✅ Em uso |
| Build | maven-dependency-plugin (carrega o Mockito como agente Java nos testes) | 3.8.1 | ✅ Em uso |
| Build | maven-jar-plugin (gera o jar executável `sistema-lanchonete.jar`) | 3.4.2 | ✅ Em uso |
| Testes | JUnit Jupiter | 5.11.4 | ✅ Em uso |
| Testes | Mockito (mockito-junit-jupiter) | 5.14.2 | ✅ Em uso |
| Cobertura | JaCoCo (relatório de cobertura e verificação mínima de 75% de linhas no build) | 0.8.12 | ✅ Em uso |
| CI | GitHub Actions (`actions/checkout`, `actions/setup-java` com Temurin 21, `actions/upload-artifact`) | v5 / v5 / v5 | ✅ Em uso |
| Contêiner | Docker (multi-stage: `maven:3.9-eclipse-temurin-21` no build e `eclipse-temurin:21-jre` na execução) | — | ✅ Em uso |
| Persistência | Repositórios em memória | — | ✅ Em uso |
| Persistência | Banco de dados relacional | — | ⏳ Planejado |
| Testes | H2 (dependência de teste no `pom.xml`, ainda não usada nos testes) | 2.3.232 | ⏳ Planejado |
| Análise estática | SonarCloud | — | ✅ Em uso |
| Prototipação | Figma (protótipo de interface) | — | ⏳ Planejado |

A stack foi definida pela equipe; a escolha do banco de dados será registrada aqui quando a persistência for implementada.

## 📁 Estrutura do repositório

```
.
├── .github/
│   └── workflows/
│       └── ci.yml            # Pipeline: build, testes, cobertura mínima, SonarCloud e Docker
├── docs/                     # planejado
│   ├── uml/                  # planejado: casos de uso, classes e sequência
│   ├── bpm/                  # planejado: fluxo de pedidos e estoque
│   └── plano-de-testes/      # planejado: plano de testes e evidências
├── src/
│   ├── main/
│   │   ├── java/br/unisul/lanchonete/   # Código-fonte
│   │   │   ├── App.java                 # Classe principal
│   │   │   ├── model/                   # Entidades
│   │   │   ├── repository/              # Acesso a dados
│   │   │   ├── service/                 # Regras de negócio
│   │   │   └── controller/              # planejado: camada de entrada
│   │   └── resources/                   # planejado: configurações e scripts SQL
│   └── test/
│       └── java/br/unisul/lanchonete/   # Testes unitários e de integração
│           ├── AppTest.java
│           ├── model/
│           ├── repository/
│           └── service/
├── .dockerignore             # Arquivos excluídos do contexto de build da imagem
├── .env.example              # Modelo das variáveis de ambiente (usadas quando houver banco)
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
| **Produtos** | id, nome, categoria, preço, quantidadeEstoque (saldo atual do produto) |
| **Clientes** | id, nome, cpf, telefone |
| **Funcionários** | id, nome, cargo, login, senha (hash) |
| **Pedidos** | id, id_cliente, valor_total, status |
| **Itens_Pedido** | id, id_pedido, id_produto, quantidade, preço_unitário |
| **Estoque** (histórico de movimentações de entrada e saída) *(planejado)* | id, id_produto, quantidade_entrada, quantidade_saída |

> **Observação:** na versão atual, o saldo é controlado diretamente em `Produto.quantidadeEstoque` pelo `EstoqueService` (verificar, dar baixa e repor). O registro histórico de cada movimentação será implementado depois.

**Relacionamentos:** um cliente tem vários pedidos; um pedido tem vários itens; cada item referencia um produto; quando o histórico for implementado, cada movimentação de estoque referenciará um produto.

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

# 3. Executar a demonstração do fluxo de pedidos
java -jar target/sistema-lanchonete.jar
```

A execução (`App.main`) roda uma demonstração completa do fluxo, sem pedir entrada pelo teclado. Ela cadastra cliente e produtos, confirma um pedido com baixa automática no estoque, recusa um pedido acima do estoque sem baixa parcial e cancela o pedido confirmado com reposição dos itens. O passo a passo e os saldos de estoque são impressos no console. O contêiner Docker executa a mesma demonstração.

> **Windows:** se os acentos aparecerem trocados no terminal, use `java -Dstdout.encoding=UTF-8 -jar target/sistema-lanchonete.jar`.

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

# Executar só os testes de integração
mvn test -Dgroups=integracao
```

| Tipo | Escopo |
|---|---|
| **Unitários** | Regras de negócio e validações, com JUnit 5 e Mockito |
| **Integração** | Serviço + repositório em memória real, sem mocks (H2: planejado) |
| **Sistema** *(planejado)* | Fluxo completo de pedido |
| **Usabilidade** *(planejado)* | Roteiro de avaliação com o protótipo |

Total atual: **183 métodos de teste** e **213 execuções** (contando os casos dos testes parametrizados).

A cobertura mínima de 75% de linhas já é exigida no build: o `mvn verify` falha se a cobertura ficar abaixo disso.

**Metas:** Quality Gate aprovado · zero issues abertos.

### Análise estática e Quality Gate

**Ferramenta:** SonarCloud, integrado ao Maven e ao GitHub Actions.

**Passos de integração**

1. Criar a organização no SonarCloud a partir do GitHub e importar o repositório.
2. Gerar o token e cadastrá-lo como secret `SONAR_TOKEN` no repositório.
3. Configurar no `pom.xml` as propriedades `sonar.organization`, `sonar.projectKey` e o caminho do relatório XML do JaCoCo (`sonar.coverage.jacoco.xmlReportPaths`).
4. Adicionar ao workflow uma etapa que roda `mvn verify sonar:sonar` após os testes.
5. Desativar a "Automatic Analysis" do SonarCloud para usar a análise via CI.

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

O workflow `CI` (`.github/workflows/ci.yml`) roda a cada `push` e `pull request` em qualquer branch.

**Job `build`**

1. **Checkout do código** (`actions/checkout`)
2. **Configurar Java 21 (Temurin)** (`actions/setup-java`, com cache do Maven)
3. **Compilar, testar e gerar cobertura**: `mvn -B verify` compila o projeto, roda os testes unitários e de integração, gera o relatório do JaCoCo e verifica a cobertura mínima de 75% de linhas, reprovando o build abaixo disso
4. **Publicar relatório do JaCoCo**: envia `target/site/jacoco` como artefato `jacoco-report`, mesmo se o build falhar

**Job `docker`** (roda depois do job `build`, só se ele passar)

1. **Checkout do código**
2. **Construir imagem Docker**: `docker build -t sistema-lanchonete .`
3. **Executar contêiner**: `docker run --rm sistema-lanchonete`

**Análise no SonarCloud:** no job `build`, depois dos testes, a etapa **Análise SonarCloud** roda `mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar` com o secret `SONAR_TOKEN`, enviando o código e o relatório de cobertura do JaCoCo para validação do Quality Gate. Ela é executada em pull requests e em pushes na `main`, com cache em `~/.sonar/cache` (ver [Análise estática e Quality Gate](#análise-estática-e-quality-gate)).

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

**Branches**

- `main` é a branch principal.
- O trabalho é feito em branches curtas, criadas a partir da `main` atualizada, com os prefixos `feat/`, `fix/`, `test/`, `refactor/`, `docs/`, `chore/` e `ci/` (os mesmos tipos da convenção de commits). Exemplos: `feat/produto`, `test/cliente-integracao`.

**Pull Request**

- Toda mudança entra na `main` por Pull Request.
- A CI precisa estar verde (testes, cobertura mínima e Docker).
- É necessária pelo menos uma revisão de outro integrante.
- A descrição referencia a issue: `Refs #N`, ou `Closes #N` quando o PR resolve a issue.

**Merge**

- Sempre com **Create a merge commit**, nunca com **Squash and merge**, para preservar os commits atômicos e a autoria de cada integrante, que é usada na avaliação.
- Depois do merge, atualizar a `main` local (`git pull`) e apagar a branch.

**Issues do professor**

- São prioridade e devem ser resolvidas antes da entrega.

**Ciclo de trabalho**

```bash
# 1. Atualizar a main
git checkout main
git pull

# 2. Criar a branch
git checkout -b feat/produto

# 3. Commitar
git add <arquivos>
git commit -m "feat(produto): adiciona entidade Produto"

# 4. Enviar e abrir o PR no GitHub
git push -u origin feat/produto

# 5. Após o merge: voltar para a main e apagar a branch
git checkout main
git pull
git branch -d feat/produto
git push origin --delete feat/produto
```

## 📊 Contribuições

| Aluno | Funcionalidade | Teste(s) unitário(s) |
|---|---|---|
| Carlos Henrique Lohn | Classe principal da aplicação | [`AppTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/AppTest.java) |
| Carlos Henrique Lohn | Entidade Cliente | Sem teste dedicado; usada em [`ClienteRepositoryEmMemoriaTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/repository/ClienteRepositoryEmMemoriaTest.java) e [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Carlos Henrique Lohn | Validação de CPF | [`CpfValidatorTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/CpfValidatorTest.java) |
| Carlos Henrique Lohn | Interface [`ClienteRepository`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/repository/ClienteRepository.java) e repositório de clientes em memória | [`ClienteRepositoryEmMemoriaTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/repository/ClienteRepositoryEmMemoriaTest.java) |
| Carlos Henrique Lohn | Cadastro de cliente com validações e bloqueio de CPF duplicado | [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Carlos Henrique Lohn | Consulta de cliente por id e por CPF e listagem | [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Carlos Henrique Lohn | Atualização de cliente | [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Carlos Henrique Lohn | Exclusão de cliente | [`ClienteServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceTest.java) |
| Carlos Henrique Lohn | Testes de integração do módulo de clientes (serviço + repositório em memória real) | [`ClienteServiceIntegracaoTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ClienteServiceIntegracaoTest.java) |
| Lucas da Costa | Enum [`StatusPedido`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/model/StatusPedido.java) (`ABERTO`, `CONFIRMADO`, `CANCELADO`) | Coberto em [`PedidoTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/model/PedidoTest.java) |
| Lucas da Costa | Entidade [`ItemPedido`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/model/ItemPedido.java) com validações e cálculo de subtotal | [`ItemPedidoTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/model/ItemPedidoTest.java) |
| Lucas da Costa | Entidade [`Pedido`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/model/Pedido.java) com controle de status e cálculo do total | [`PedidoTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/model/PedidoTest.java) |
| Lucas da Costa | Interface [`PedidoRepository`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/repository/PedidoRepository.java) e [`PedidoRepositoryEmMemoria`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/repository/PedidoRepositoryEmMemoria.java) | [`PedidoRepositoryEmMemoriaTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/repository/PedidoRepositoryEmMemoriaTest.java) |
| Lucas da Costa | Bloqueio de transições inválidas de status e de adição de itens em pedido fechado (Issue #18) | [`PedidoTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/model/PedidoTest.java) |
| Lucas da Costa | [`PedidoService`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/service/PedidoService.java): criação, adição de itens, confirmação com baixa sem baixa parcial e cancelamento com reposição | [`PedidoServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/PedidoServiceTest.java) |
| Lucas da Costa | Testes de integração do fluxo de pedidos (serviços + repositórios em memória reais) | [`PedidoServiceIntegracaoTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/PedidoServiceIntegracaoTest.java) |
| Lucas da Costa | Consolidação da execução em [`App`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/App.java): demonstração do fluxo completo de pedidos e estoque | [`AppTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/AppTest.java) |
| Murilo dos Santos Ferreira | Entidade [`Produto`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/model/Produto.java) | [`ProdutoTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/model/ProdutoTest.java) |
| Murilo dos Santos Ferreira | Interface [`ProdutoRepository`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/repository/ProdutoRepository.java) e [`ProdutoRepositoryEmMemoria`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/repository/ProdutoRepositoryEmMemoria.java) | [`ProdutoRepositoryEmMemoriaTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/repository/ProdutoRepositoryEmMemoriaTest.java) |
| Murilo dos Santos Ferreira | [`ProdutoService`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/service/ProdutoService.java): cadastro com validações, consulta por id e listagem, com [`ProdutoNaoEncontradoException`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/service/ProdutoNaoEncontradoException.java) | [`ProdutoServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/ProdutoServiceTest.java) |
| Murilo dos Santos Ferreira | [`EstoqueService`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/service/EstoqueService.java): verificação, baixa e reposição de estoque, com [`EstoqueInsuficienteException`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/main/java/br/unisul/lanchonete/service/EstoqueInsuficienteException.java) | [`EstoqueServiceTest`](https://github.com/LucasdaCosta04/System-Lanchonete/blob/main/src/test/java/br/unisul/lanchonete/service/EstoqueServiceTest.java) |

### Commits por integrante

| Aluno | Commits de código-fonte | Principais commits |
|---|---|---|
| Carlos Henrique Lohn | 20 | [`97ff666`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/97ff66691358fd1dd6735c014b0fcfa4d9aa4c34) entidade Cliente · [`c06a33a`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/c06a33a41d125cbb38c5c1677f2b1d10950bfdb3) validação de CPF · [`f75f94b`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/f75f94b565a474e58e55ec2e69e82891a5bcb158) repositório em memória · [`e3aaf31`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/e3aaf314eafd422a6c9bf9dd6d4adbb8fb76b664) cadastro com validações · [`11b1414`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/11b14142cdb581c42c16bd38b8992cb94ef3d6f3) bloqueio de CPF duplicado · [`dc7b364`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/dc7b3644668b51f80e6fffd0b183091952c645f1) consultas de cliente · [`26a88de`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/26a88de14e62204404a984a405320539b183dd42) atualização de cliente · [`14a147b`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/14a147bf0549cc4f415bb282573de8ca1d06a4ef) exclusão de cliente · [`8bd8d25`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/8bd8d253c3464ccd956e8cfabe51eb30876ac017) integração: cadastro e consulta · [`392edcd`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/392edcd0cbc915357bc9a3e4da052877adf449e3) integração: atualização e exclusão · [`3681901`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/368190102affde81d689b725c4f1f31b6067c5de) integração: ciclo completo |
| Lucas da Costa | 14 | [`10eb6c0`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/10eb6c0c0ae625fb9d2948029102e0fa45d4153e) enum StatusPedido · [`6886668`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/68866680d42c62e04c33972d2628400784479cda) ItemPedido e subtotal · [`d249272`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/d249272f4e1f9b7271ddf04429555da2832cf1a6) Pedido e cálculo do total · [`f7b4b2f`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/f7b4b2f7541920c37953de5cc66f8e4a7c0d3f0d) PedidoRepository em memória · [`a890e28`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/a890e28d75ad3d5417292e670ed99921ab815f62) correção apontada pelo SonarCloud · [`8cef0bf`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/8cef0bfc7f0839ff1ce2ab8632184f8ecd26ba1a) bloqueio de transições inválidas · [`73ec46f`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/73ec46fab06e5eaf7c8139b6752889ed8dc21eb3) PedidoService · [`75fc190`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/75fc19030967b2e047f080b73a6a114b61b27f28) testes unitários do PedidoService · [`eea6958`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/eea69585328476ebee8186e140465178b447149b) testes de integração do fluxo de pedidos · [`b03482b`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/b03482b359badcb5d68517027a5162885d53abbc) fluxo demonstrativo no App.main |
| Murilo dos Santos Ferreira | 14 | [`95fb48e`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/95fb48e43ef85e5e65fd56ac7d350eae78aebc67) entidade Produto · [`0d29a26`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/0d29a26383c05bb61e0df3b10e767de141312937) interface ProdutoRepository · [`881adb7`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/881adb7c4baa9826673e1f68add57ca670472706) repositório de produtos em memória · [`295ea70`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/295ea7017b93f4ecdba28f367c72443c28546b60) cadastro de produto com validações · [`1fdea1e`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/1fdea1ef0d8b981af5e2113d13663344cae94db0) consultas de produto · [`397bf08`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/397bf08743a81240a409df3a5e395b7a0cbde88a) EstoqueInsuficienteException · [`1aced49`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/1aced49627af460cdef6b30836bf3168fa04bb8d) verificação e baixa de estoque · [`af6021c`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/af6021cb9f40ccaf0af2e33afc5981d230e02fd5) reposição de estoque · [`bd019f8`](https://github.com/LucasdaCosta04/System-Lanchonete/commit/bd019f82419d84d2d6c08ed75897f26f6fc4b747) testes de reposição de estoque |

> As tabelas são atualizadas a cada entrega. Só são considerados commits que alteram código-fonte (`src/`) na branch `main`.

## 👨‍💻 Equipe

| Nome | Papel | Módulo | GitHub |
|---|---|---|---|
| _Lucas da Costa_ | Líder de equipe | Pedidos | [@LucasdaCosta04](https://github.com/LucasdaCosta04) |
| _Carlos Henrique Lohn_ | Desenvolvedor | Clientes | [@carloshlohn](https://github.com/carloshlohn) |
| _Murilo dos Santos Ferreira_ | Desenvolvedor | Produto e Estoque; depois Funcionários e Login | [@MuriloS-Ferreira](https://github.com/MuriloS-Ferreira) |

**Curso:** _Ciência da Computação_ · **Semestre:** _6_ · **Professor:** Jorge Werner

## 📄 Licença

Distribuído sob a licença MIT. Veja o arquivo [`LICENSE`](LICENSE) para mais detalhes.
