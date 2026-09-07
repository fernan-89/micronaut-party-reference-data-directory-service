# ADR 008: Reactive MDC Context Propagation Bridge

## Status
Accepted

## Context
Non-blocking reactive streams hop across worker threads (e.g. Netty event loops to Reactor scheduler threads), causing standard ThreadLocal-based SLF4J MDC states to be lost (`[trace=NONE]`).

## Decision
Implement a custom `ReactorMdcBridge` using `Hooks.onEachOperator` lifting subscribers to synchronize Reactor execution context variables (`traceId`, `clientIp`, `userAgent`, `virtualHost`, `clientOrigin`, `externalClientHost`) into the physical thread's SLF4J MDC before any reactive signal is delivered.
