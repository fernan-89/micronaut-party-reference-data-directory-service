# ADR 012: Execution Context Forensics & SRE Telemetry

## Status
Accepted

## Context
Containers starting up or failing prematurely often emit blank or untracked logs prior to HTTP requests.

## Decision
Inject pre-flight boot context (`SYSTEM-BOOT`) and shutdown context (`SYSTEM-SHUTDOWN`) into MDC at bootstrap and shutdown phases, dynamically discover execution environment (Kubernetes Pod / Docker Container / Bare-Metal), and resolve external hostnames off Netty event loops via asynchronous reverse DNS.
