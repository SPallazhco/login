# Fase 1: Construcción
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Fase 2: Ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/app.jar /app.jar
EXPOSE 8787
ENTRYPOINT ["java", "-jar", "/app.jar"]