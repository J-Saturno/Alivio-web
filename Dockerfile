# Etapa 1: compilar Alivio con Maven + Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Etapa 2: ejecutar Alivio con Java 21
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/alivio-web-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]