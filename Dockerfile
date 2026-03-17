# Stage 1: Build
FROM docker.io/maven:3-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia só o pom e baixa dependências (cache friendly)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto e builda
COPY . .
RUN mvn clean package

# Stage 2: Runtime
FROM docker.io/eclipse-temurin:21

WORKDIR /app

# Cria diretório de logs
RUN mkdir -p logs

# Copia o JAR do stage anterior (usa wildcard pra garantir)
COPY --from=builder /app/target/*.jar app.jar

# Porta que o Spring Boot vai usar
EXPOSE 8080

# Entry point
ENTRYPOINT ["java","-jar","/app/app.jar"]