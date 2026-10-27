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
| RF02 | Cadastrar, consultar, atualizar e excluir clientes | 🟡 Parcial (cadastro e consulta prontos; atualização e exclusão pendentes) |
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

**Planejado**

- Análise no SonarCloud e Quality Gate
- Banco de dados e testes de integração com H2
- Interface e camada de entrada (`controller/`), incluindo a execução pela linha de comando
- Atualização e exclusão de clientes
- Módulos de Produtos, Estoque, Pedidos, Pagamento, Funcionários (login) e Relatórios
- Documentação em `docs/` (UML, BPM e plano de testes)
- Dockerfile

## 🔄 Fluxo principal e critérios de aceitação

**Fluxo:** o atendente faz login → seleciona ou cadastra o cliente → adiciona produtos ao pedido → confirma → o estoque é atualizado → o pagamento é registrado.

**Critérios de aceitação:**

- [ ] Pedido com estoque suficiente é criado e o estoque diminui na quantidade exata.
- [ ] Pedido com estoque insuficiente é rejeitado com mensagem clara e sem alterar o estoque.
- [ ] Pedido cancelado devolve os itens ao estoque.
- [ ] Valor total do pedido é igual à soma de `quantidade × preço unitário` dos itens.
- [ ] Login com credenciais inválidas é negado.

## 🛠 Tecnologias

| Camada | Tecnologia | Versão | Status |
|---|---|---|---|
| Linguagem | Java | 21 | ✅ Em uso |
| Build | Maven (maven-compiler-plugin / maven-surefire-plugin) | 3.13.0 / 3.5.2 | ✅ Em uso |
| Testes | JUnit Jupiter | 5.11.4 | ✅ Em uso |
| Testes | Mockito (mockito-junit-jupiter) | 5.14.2 | ✅ Em uso |
| Cobertura | JaCoCo | 0.8.12 | ✅ Em uso |
| CI | GitHub Actions (`actions/checkout`, `actions/setup-java` com Temurin 21, `actions/upload-artifact`) | v5 / v5 / v5 | ✅ Em uso |
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

## ⚙️ Integração contínua

A cada `push` e `pull request` para a `main`, o GitHub Actions:

1. Compila o projeto
2. Executa os testes unitários
3. Gera o relatório de cobertura (JaCoCo)
4. Envia a análise ao SonarCloud e valida o Quality Gate *(planejado)*

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
| _Lucas da Costa_ | Líder de equipe | Pedidos | [@LucasdaCosta04](https://github.com/LucasdaCosta04) |
| _Carlos Henrique Lohn_ | Desenvolvedor | Clientes | [@carloshlohn](https://github.com/carloshlohn) |
| _Murilo dos Santos Ferreira_ | Desenvolvedor | Funcionários e Login | [@MuriloS-Ferreira](https://github.com/MuriloS-Ferreira) |

**Curso:** _Ciência da Computação_ · **Semestre:** _6_ · **Professor:** Jorge Werner

## 📄 Licença

Distribuído sob a licença MIT. Veja o arquivo [`LICENSE`](LICENSE) para mais detalhes.
