# --- STAGE 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests

# --- STAGE 2: Runtime image ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/ecommerce-backend-1.0.jar /app/ecommerce-backend.jar

EXPOSE 8080

CMD ["java", "-jar", "ecommerce-backend.jar"]
