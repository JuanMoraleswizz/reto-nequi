# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Instalar Maven (eclipse-temurin no lo incluye)
RUN apk add --no-cache maven

# Copiar solo el pom.xml primero para aprovechar la caché de capas de Docker
# (las dependencias no se re-descargan si solo cambia el código fuente)
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copiar el código fuente y construir el jar
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copiar solo el jar desde el stage de build
COPY --from=builder /build/target/franchises-*.jar app.jar

# Puerto expuesto (documentativo, no publica el puerto automáticamente)
EXPOSE 8080

# Health check para Docker y orquestadores
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Opciones JVM para contenedores:
# -XX:+UseContainerSupport      → detecta límites de CPU/memoria del contenedor
# -XX:MaxRAMPercentage=75.0     → usa máximo el 75% de la RAM asignada al contenedor
# -Djava.security.egd=...       → acelera el inicio en entornos sin /dev/random robusto
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
