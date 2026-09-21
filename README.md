# Thinklab Party Reference Data Directory Service

**Version:** v2.0.0-BIAN

**Status:** Production-Ready (Mission-Critical)

## Overview

The Thinklab Party Reference Data Directory Service (formerly "Company Service") is a mission-critical, high-assurance microservice engineered for the authoritative management of the complete lifecycle of corporate entities (`Organisation`), their subordinate units (`OrganisationUnit`, formerly "Branch"), contacts, and billing configurations. It implements the BIAN (Banking Industry Architecture Network) `party-reference-data-directory` Service Domain: every route follows the `/{behavior-qualifier}` convention (`initiate`, `retrieve`, `update`, `control`) instead of ad-hoc REST CRUD verbs — see ADR-013/014/015. Developed using Java 21 and the Micronaut Framework, this service utilizes strict Hexagonal Architecture (Ports and Adapters) combined with a fully Reactive Stack to ensure high throughput, zero-blocking I/O, and absolute structural maintainability.

Designed under strict Site Reliability Engineering (SRE) and Zero-Trust principles, the service features deterministic containerization, Ahead-of-Time (AOT) bytecode optimizations, and resilient telemetry pipelines capable of surviving transient infrastructure failures.

## Technology Stack

* **Runtime:** Java 21 LTS (Project Loom / Virtual Threads enabled)
* **Framework:** Micronaut 4.4.2 (AOT Optimized, reflection-free DI and Serde)
* **Reactive Engine:** Project Reactor (Mono / Flux)
* **Persistence:** Reactive MongoDB utilizing BSON Binary UUID Subtype 4 for optimized indexing
* **Observability:** W3C Trace Context, SLF4J, Logback (Async), SRE Forensics, and Project Reactor Hooks
* **Security & Containerization:** Google Distroless (nonroot), Read-Only Root Filesystems, Zero-Trust Capabilities
* **Testing Suite:** JUnit 5, Mockito (Unit), and Testcontainers (Integration)
* **Documentation:** OpenAPI 3.0 / Swagger (Generated statically at compile-time)

---

## Architectural Model & Engineering Mandates

The project implements a strict three-tier hexagonal structure to ensure that the Core Domain remains framework-agnostic and immune to infrastructure volatility:

```text
src/main/java/com/thinklab/
├── domain/                  # Core Business Logic (Rich Aggregate Roots, Value Objects, Domain Enums)
├── application/             # Orchestration & Use Cases (Input/Output DTOs, Mappers, Use Cases)
└── infrastructure/          # External Integrations (REST Controllers, MongoDB Adapters, Telemetry, Health)
```

### 1. Hexagonal & DTO Isolation Pattern (ADR-001, ADR-003)

The Core Domain is completely decoupled from web and persistence layers.

* **Domain Purity:** Domain entities (`Organisation`) are devoid of `@Serdeable`, `@Schema`, or `@MappedEntity` annotations.
* **Projection Mapping:** All input/output crosses boundaries via explicit Data Transfer Objects (DTOs) utilizing static factory transformations (`OrganisationMapper`).
* **Resilient Exception Boundaries:** Structural decoupling of Business Exceptions from Infrastructure Failures. All errors are projected into standardized **RFC 7807 (Problem Details)** payloads via a unified `GlobalExceptionHandler`.

### 2. Identity Sovereignty & Partial State Mutations (ADR-002, ADR-005)

* **UUID Standardization:** Mandatory enforcement of native `java.util.UUID` for all primary and correlation identifiers to ensure optimal MongoDB BSON indexing and prevent type contamination.
* **Explicit Partial Updates:** Monolithic saves are strictly reserved for aggregate creation. State mutations (e.g., status change, basic info update, adding organisation units/contacts, updating billing) utilize targeted `$set` and `$push` query updates (`_id` bounded) via custom repository ports. There is no physical delete — `control/cancel` is a terminal, soft status transition (ADR-014).

### 3. Proactive Initialization & Synchronous Barriers (ADR-006, ADR-007)

The application adheres to a "Fail-Fast before Port Binding" philosophy to protect Kubernetes routing.

* **Two-Phase Warmup:** A `StartupEvent` triggers a deterministic MongoDB ping to both `admin` and dynamically parsed application databases, forcing SDAM topology discovery.
* **Passive Circuit Breaking:** Progressive backoff retry strategies (15s, 30s, 60s) before shutting down gracefully if dependencies are unreachable.

### 4. Distributed Telemetry & Privacy-Preserving Logging (ADR-008, ADR-009, ADR-010)

End-to-end traceability across non-blocking asynchronous thread boundaries is guaranteed without violating data privacy regulations.

* **W3C Trace Context:** Natively decodes standard `traceparent` headers without fragile third-party SDK dependencies.
* **Reactive MDC Bridge:** Synchronizes Project Reactor's `Context` with SLF4J's `MDC` across worker thread hops.
* **Privacy by Design (LGPD/GDPR):** Obfuscates client IP addresses (`192.168.1.***`) and truncates long User-Agents before writing to logs.

### 5. Zero-Trust Containerization & AOT Packaging (ADR-011)

* **Distroless & Non-Root Execution:** The production image (`gcr.io/distroless/java21-debian12:nonroot`) executes strictly as UID 65532 without shell access.
* **Immutable Filesystems:** Kubernetes deployments enforce `readOnlyRootFilesystem: true` and drop `ALL` capabilities.

---

## Operational Procedures

### Local Build and Execution

```bash
# Clean, resolve dependencies, run AOT optimizations, and assemble Fat JAR
./gradlew clean build

# Start the Reactive Service (Default Port: 8081)
./gradlew run
```

### Docker & Kubernetes (Cloud-Native Build)

```bash
# Build the Distroless Container Image
docker build -t thinklab-party-reference-data-directory-service:latest .

# Apply Kubernetes strict deployment manifest
kubectl apply -f k8s-deployment.yaml
```

### Infrastructure and API Endpoints

* **Health and Readiness Probes:** `http://localhost:8081/health`
* **Swagger UI (Interactive API Contract):** `http://localhost:8081/swagger-ui`
* **OpenAPI Specs (Raw YAML):** `http://localhost:8081/swagger/thinklab-party-reference-data-directory-service-domain-v2.0.0.yml`

### BIAN Behavior Qualifier Contract (`/party-reference-data-directory/v1`)

All mutations require the `X-Executor` header. There is no `DELETE` — `control/cancel` replaces the previous physical deletion.

| Behavior Qualifier | Method & Path |
|---|---|
| initiate | `POST /party-reference-data-directory/v1/initiate` |
| retrieve (single) | `GET /party-reference-data-directory/v1/{id}/retrieve` |
| retrieve (collection) | `GET /party-reference-data-directory/v1/retrieve` |
| update | `PUT /party-reference-data-directory/v1/{id}/update` |
| control/activate, suspend, cancel | `PUT /party-reference-data-directory/v1/{id}/control/{action}` |
| billing/update | `PUT /party-reference-data-directory/v1/{id}/billing/update` |
| organisation-unit/initiate | `POST /party-reference-data-directory/v1/{id}/organisation-unit/initiate` |
| organisation-unit/retrieve (collection & single) | `GET /party-reference-data-directory/v1/{id}/organisation-unit/retrieve`, `GET .../organisation-unit/{unitId}/retrieve` |
| organisation-unit/control/suspend, reactivate | `PUT /party-reference-data-directory/v1/{id}/organisation-unit/{unitId}/control/{action}` |
| contact/initiate | `POST /party-reference-data-directory/v1/{id}/contact/initiate` |
| contact/retrieve | `GET /party-reference-data-directory/v1/{id}/contact/retrieve` |

Example:

```bash
curl -X POST http://localhost:8081/party-reference-data-directory/v1/initiate \
  -H "Content-Type: application/json" \
  -H "X-Executor: admin-user-01" \
  -d '{"corporateName":"Acme Corp","tradeName":"Acme","taxIdentifier":"12345678000199","billing":{"billingEmail":"billing@acme.com","currency":"USD","taxRegime":"SIMPLES"}}'
```

## License

Proprietary - all rights reserved. See [LICENSE](LICENSE). This software is not open source.
