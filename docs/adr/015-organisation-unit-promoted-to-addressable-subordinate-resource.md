# ADR 015: OrganisationUnit Promoted to Addressable Subordinate Resource

## Status
Accepted

## Context
The former `Branch` value object had no identity-addressable API surface (no individual GET, no status) and no lifecycle of its own — once added to a Company, it could never be suspended or reactivated independently. The platform blueprint's original Tenant Service design modeled Branch tenants with their own `ACTIVE`/`SUSPENDED` state, and BIAN's Behavior Qualifier Instance Record pattern expects exactly this: a sub-entity that stays inside its parent aggregate's consistency boundary but is independently retrievable and controllable through the API.

## Decision
`OrganisationUnit` (formerly `Branch`) gains an `OrganisationUnitStatus` field (`ACTIVE` / `SUSPENDED`, default `ACTIVE`) and is exposed as an individually addressable resource under its parent Organisation:
- `GET /party-reference-data-directory/v1/{id}/organisation-unit/retrieve` — list all units of an Organisation
- `GET /party-reference-data-directory/v1/{id}/organisation-unit/{unitId}/retrieve` — retrieve one unit (404 via `OrganisationNotFoundException` if absent)
- `PUT /party-reference-data-directory/v1/{id}/organisation-unit/{unitId}/control/suspend` and `.../control/reactivate`

The unit remains embedded inside the `Organisation` MongoDB document (no separate collection) — mutations go through `$push` (create) and a positional `$` update (`organisationUnits.$.status`) filtered by `(organisationId, unitId)`, keeping the aggregate as the single consistency boundary while the API treats the unit as its own resource.
