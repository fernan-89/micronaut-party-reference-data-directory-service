# ADR 002: Atomic Partial State Mutations in Reactive Persistence

## Status
Accepted

## Context
Aggregates in MongoDB risk race conditions and accidental overwrites if entire document snapshots are serialized on every update.

## Decision
Monolithic `.save()` operations are strictly reserved for aggregate creation. All state transitions (updating status, adding branches, updating contacts, editing billing info) must use atomic MongoDB operators (`$set`, `$push`) targeted strictly by sovereign UUID (`_id`).

## Consequences
- Prevents concurrent field clobbering.
- Minimizes network I/O and serialize/deserialize latency.
