# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent POM and common-lib first for dependency caching
COPY pom.xml ./
COPY common-lib/pom.xml ./common-lib/
COPY auth-service/pom.xml ./auth-service/
COPY trade-service/pom.xml ./trade-service/
COPY market-data-service/pom.xml ./market-data-service/
COPY analytics-service/pom.xml ./analytics-service/
COPY api-gateway/pom.xml ./api-gateway/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl common-lib,auth-service -am -B

# Copy source code
COPY common-lib/src ./common-lib/src
COPY auth-service/src ./auth-service/src

# Build
RUN mvn clean package -pl common-lib,auth-service -am -Dmaven.test.skip=true -B

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from build stage
COPY --from=build /app/auth-service/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
