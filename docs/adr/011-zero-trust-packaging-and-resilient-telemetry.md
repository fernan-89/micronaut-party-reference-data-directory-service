# ADR 011: Zero-Trust Packaging & Resilient Telemetry

## Status
Accepted

## Context
Fragile third-party tracing SDKs often cause runtime class loading failures (`NoClassDefFoundError`) and introduce heavy security footprints in runtime containers.

## Decision
1. Purge direct OpenTelemetry SDK coupling; extract W3C `traceparent` natively via `TraceIdFilter`.
2. Package container using Google Distroless (`gcr.io/distroless/java21-debian12:nonroot`) with UID 65532, zero shell access, and immutable read-only root filesystems.
