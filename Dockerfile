# ---------- Etapa 1: build com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia só o pom.xml e baixa as dependências: esta camada fica em cache
# enquanto o pom.xml não mudar
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copia o código e roda testes + verificação de cobertura (JaCoCo)
COPY src ./src
RUN mvn -B verify

# ---------- Etapa 2: execução só com JRE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuário sem privilégios para não rodar a aplicação como root
RUN groupadd --system app && useradd --system --gid app --no-create-home app

# Copia da etapa de build apenas o JAR final
COPY --from=build /app/target/sistema-lanchonete.jar app.jar

USER app

ENTRYPOINT ["java", "-jar", "app.jar"]
