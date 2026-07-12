# =============================================================================
# Stage 1 — Build with Maven Wrapper
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven Wrapper first (leverages Docker cache for dependencies)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build the JAR (skip tests)
COPY src src
RUN ./mvnw clean package -DskipTests -B

# =============================================================================
# Stage 2 — Lightweight Runtime
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the fat JAR from the build stage
COPY --from=builder /build/target/*.jar app.jar

# Create uploads directory (runtime data)
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
