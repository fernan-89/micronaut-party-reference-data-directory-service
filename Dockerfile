# ==============================================================================
# File: Dockerfile
# Purpose: Zero-Trust Containerization for Company Service
# ==============================================================================

# Stage 1: Build & AOT Compilation
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Gradle wrappers and config for dependency caching layer
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and perform optimized build
COPY src ./src
RUN ./gradlew shadowJar -x test --no-daemon

# Stage 2: Runtime (Zero-Trust Distroless)
FROM gcr.io/distroless/java21-debian12:nonroot

# Labeling for SRE & DevOps Observability
LABEL maintainer="Thinklab Enterprise SRE"
LABEL version="0.1.0-SNAPSHOT"
LABEL description="Company Service - Strict Hexagonal Architecture"

WORKDIR /app

# Copy the generated AOT shadow jar with strict ownership
COPY --from=builder --chown=65532:65532 /build/build/libs/*-all.jar /app/application.jar

# Enforce Non-Root Execution (UID 65532 is standard for Distroless)
USER 65532:65532

# Expose HTTP port (Configured via application.yml)
EXPOSE 8080

# SRE: Container-Aware JVM Tuning
# 75% RAM limit prevents OOMKills in k8s. G1GC is optimal for response times.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# ==============================================================================
# SECCOMP & KUBERNETES REQUIREMENTS:
# This image MUST be deployed with the following securityContext:
# securityContext:
#   readOnlyRootFilesystem: true
#   runAsNonRoot: true
#   runAsUser: 65532
#   allowPrivilegeEscalation: false
#   capabilities:
#     drop:
#       - ALL
# ==============================================================================

ENTRYPOINT ["java", "-jar", "/app/application.jar"]