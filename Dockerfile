# Estágio de Build
FROM eclipse-temurin:25-jdk-alpine AS build
COPY . /app
WORKDIR /app
# Dá permissão de execução ao wrapper e compila o projeto
RUN chmod +x mvnw
RUN ./mvnw package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:25-jre-alpine
COPY --from=build /app/target/quarkus-app/ /deployments/

# O Render injeta a variável PORT dinamicamente, o Quarkus precisa escutá-la
ENV QUARKUS_HTTP_HOST=0.0.0.0
ENV QUARKUS_HTTP_PORT=${PORT:-8081}

EXPOSE ${QUARKUS_HTTP_PORT}

CMD ["java", "-jar", "/deployments/quarkus-run.jar"]