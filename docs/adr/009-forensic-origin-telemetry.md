# ADR 009: Forensic Origin Telemetry and Privacy-Preserving Logging

## Status
Accepted

## Context
Production troubleshooting requires enriched origin metadata (client IP, User-Agent, target host, referer) in application logs. However, logging raw IP addresses violates global data privacy regulations (LGPD/GDPR).

## Decision
The ingress `TraceIdFilter` extracts and binds `virtualHost`, `clientOrigin`, and `externalClientHost` (via asynchronous reverse DNS) into MDC and Reactor `Context`. To comply with LGPD/GDPR, client IP addresses are obfuscated on the last octet (e.g. `192.168.1.***`) and long User-Agents are safely truncated before reaching log sinks.
