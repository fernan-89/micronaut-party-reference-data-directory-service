# ADR 007: Synchronous Barriers for Pre-Flight Validation

## Status
Accepted

## Context
Asynchronous microservices running on Netty can open HTTP ports before internal caching, DNS resolution, and connection pools are fully primed, serving 500 errors to early traffic.

## Decision
Enforce synchronous blocking barriers during the `StartupEvent` phase (e.g. in `MongoWarmupObserver` and `ExternalEndpointsHealthIndicator`). The Netty HTTP server will not bind to ports or accept traffic until all pre-flight barriers complete successfully.
