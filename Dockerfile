# ── Stage 1: Build ──────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml dulu untuk cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code dan build
COPY src ./src
RUN mvn clean package -DskipTests

# ── Stage 2: Run ────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy jar dari stage build
COPY --from=builder /app/target/*.jar app.jar

# Port yang diexpose
EXPOSE 8080

# Jalankan aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]