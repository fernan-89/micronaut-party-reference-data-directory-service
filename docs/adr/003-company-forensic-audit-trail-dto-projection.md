# ADR 003: Non-Blocking Observability & Telemetry Pipeline

## Status
Accepted

## Context
High-throughput reactive event loops (Netty / Project Reactor) can easily suffer thread starvation if logging operations perform blocking disk I/O or synchronous console writes.

## Decision
Configure Logback with asynchronous appenders (`AsyncAppender`, `neverBlock=true`, `queueSize=2048`, `discardingThreshold=400`) and JSON structuring (`LogstashEncoder`) for ELK / Datadog.
