# 🍔 Sistema de Gestão de Lanchonetes

[![CI](https://github.com/ORGANIZACAO/REPOSITORIO/actions/workflows/ci.yml/badge.svg)](https://github.com/ORGANIZACAO/REPOSITORIO/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=PROJECT_KEY&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=PROJECT_KEY)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=PROJECT_KEY&metric=coverage)](https://sonarcloud.io/summary/new_code?id=PROJECT_KEY)

Sistema para gerenciar a operação de uma lanchonete: cadastro de produtos, pedidos, controle de estoque, clientes, funcionários e relatórios.

Projeto desenvolvido para a **A3 da unidade curricular Gestão e Qualidade de Software**, UNISUL, sob orientação do professor Jorge Werner.

---

## 📋 Sumário

- [Problema atendido](#-problema-atendido)
- [Atores](#-atores)
- [Requisitos](#-requisitos)
- [Fluxo principal e critérios de aceitação](#-fluxo-principal-e-critérios-de-aceitação)
- [Tecnologias](#-tecnologias)
- [Estrutura do repositório](#-estrutura-do-repositório)
- [Modelo de dados](#-modelo-de-dados)
- [Como executar](#-como-executar)
- [Testes e qualidade](#-testes-e-qualidade)
- [Integração contínua](#-integração-contínua)
- [Convenção de commits](#-convenção-de-commits)
- [Fluxo de trabalho com Git](#-fluxo-de-trabalho-com-git)
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

| ID | Requisito |
|---|---|
| RF01 | Cadastrar, consultar, atualizar e excluir produtos |
| RF02 | Cadastrar, consultar, atualizar e excluir clientes |
| RF03 | Criar pedidos com um ou mais itens e acompanhar o status |
| RF04 | Cancelar pedidos |
| RF05 | Controlar entradas e saídas de estoque |
| RF06 | Dar baixa automática no estoque ao confirmar um pedido |
| RF07 | Autenticar funcionários por login e senha |
| RF08 | Registrar o pagamento do pedido |
| RF09 | Gerar relatórios de vendas e de estoque |

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

## 🔄 Fluxo principal e critérios de aceitação

**Fluxo:** o atendente faz login → seleciona ou cadastra o cliente → adiciona produtos ao pedido → confirma → o estoque é atualizado → o pagamento é registrado.

**Critérios de aceitação:**

- [ ] Pedido com estoque suficiente é criado e o estoque diminui na quantidade exata.
- [ ] Pedido com estoque insuficiente é rejeitado com mensagem clara e sem alterar o estoque.
- [ ] Pedido cancelado devolve os itens ao estoque.
- [ ] Valor total do pedido é igual à soma de `quantidade × preço unitário` dos itens.
- [ ] Login com credenciais inválidas é negado.

## 🛠 Tecnologias

> ⚠️ Stack sujeita à confirmação com o professor.

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Build | Maven |
| Testes | JUnit 5, Mockito |
| Cobertura | JaCoCo |
| Análise estática | SonarCloud |
| CI | GitHub Actions |
| Banco de dados | H2 (testes) / PostgreSQL ou SQLite (execução) |
| Prototipação | Figma |

## 📁 Estrutura do repositório

```
.
├── .github/
│   └── workflows/
│       └── ci.yml            # Pipeline: build, testes, cobertura e Sonar
├── docs/
│   ├── uml/                  # Casos de uso, classes e sequência
│   ├── bpm/                  # Fluxo de pedidos e estoque
│   └── plano-de-testes/      # Plano de testes e evidências
├── src/
│   ├── main/java/            # Código-fonte
│   │   ├── model/            # Entidades
│   │   ├── repository/       # Acesso a dados
│   │   ├── service/          # Regras de negócio
│   │   └── controller/       # Camada de entrada
│   ├── main/resources/       # Configurações e scripts SQL
│   └── test/java/            # Testes unitários e de integração
├── .gitignore
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
- Maven 3.9+
- Git

### Passo a passo

```bash
# 1. Clonar o repositório
git clone https://github.com/ORGANIZACAO/REPOSITORIO.git
cd REPOSITORIO

# 2. Compilar e baixar dependências
mvn clean install

# 3. Executar a aplicação
mvn exec:java
```

### Variáveis de ambiente

Copie `.env.example` para `.env` e ajuste os valores. **Nunca** versione credenciais.

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

## ⚙️ Integração contínua

A cada `push` e `pull request` para a `main`, o GitHub Actions:

1. Compila o projeto
2. Executa os testes unitários
3. Gera o relatório de cobertura (JaCoCo)
4. Envia a análise ao SonarCloud e valida o Quality Gate

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

## 👨‍💻 Equipe

| Nome | Papel | Módulo | GitHub |
|---|---|---|---|
| _Nome 1_ | Líder de equipe | Pedidos | [@usuario](https://github.com/usuario) |
| _Nome 2_ | Especialista em DevOps | Estoque | [@usuario](https://github.com/usuario) |
| _Nome 3_ | Testador | Produtos | [@usuario](https://github.com/usuario) |
| _Nome 4_ | Desenvolvedor | Clientes | [@usuario](https://github.com/usuario) |
| _Nome 5_ | Desenvolvedor | Funcionários e Login | [@usuario](https://github.com/usuario) |

**Curso:** _Nome do curso_ · **Semestre:** _X_ · **Professor:** Jorge Werner

## 📄 Licença

Distribuído sob a licença MIT. Veja o arquivo [`LICENSE`](LICENSE) para mais detalhes.
