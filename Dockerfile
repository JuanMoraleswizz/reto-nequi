# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-17-alpine AS build

WORKDIR /app

# Descarga dependencias primero (capa cacheada mientras pom.xml no cambie)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Compila y empaqueta sin ejecutar tests
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM amazoncorretto:17-alpine AS runtime

WORKDIR /app

# Usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
