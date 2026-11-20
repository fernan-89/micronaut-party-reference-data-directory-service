# ADR 016: The by-id routes answer only for the caller's own organisation

## Status
Accepted (2026-11-20). Part of the platform tenant audit, group B (`thinklab-platform/docs/TENANT-AUDIT-2026-11.md`); the same rule as the other services (workflow-approval ADR-037 and the nine of group A), adapted to the one service where the tenant is the resource.

## Context
In this service the Organisation is the tenant: its id is the `X-Tenant-Id` of every other service. The by-id routes (retrieve, update, control, billing, units, contacts) took only the id, and the collection `GET /retrieve` listed every organisation. Any tenant could read, edit, suspend or cancel another organisation knowing its UUID, see its units and contacts, or list all of them.

## Decision
- Every route that takes an organisation id also takes `X-Tenant-Id` (required, 400 if blank), and answers only when the id **is** the tenant. Another organisation is the same as none: 404 `OrganisationNotFoundException`, nothing is read or written (`OrganisationScope`). There is no platform-operator role for now; if support or billing need to cross tenants, it gets its own ADR and role first.
- `GET /retrieve` becomes the caller's own organisation as a list of zero or one; it never lists the others. `findAll` is gone from the repository port.
- `POST /initiate` stays without a tenant: it is the onboarding that creates one.

## Consequences
- Positive: a UUID is no longer enough to read or change another organisation; the directory cannot be enumerated.
- Negative: a client that sends no tenant on these routes now gets a 400, and nobody can list all organisations. The platform scripts and the gateway smoke that listed them send their own tenant.
