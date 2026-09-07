# ADR 010: Adoption of OpenTelemetry and W3C Trace Context for Distributed Observability

## Status
Accepted

## Context
Cross-service call flows require unified distributed correlation IDs across microservice meshes (e.g. Istio, API Gateways, and downstream services).

## Decision
Support W3C Trace Context standard (`traceparent` header `00-{traceId}-{spanId}-01`) alongside Zipkin `X-B3-TraceId`. If absent, generate a traceable fallback token (`UNTRACED-{shortUuid}`). Extract trace identifiers without hard-coupling to brittle OpenTelemetry SDK dependencies.
