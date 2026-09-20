# ADR 014: Rename Company → Organisation / Party Reference Data Directory Alignment

## Status
Accepted

## Context
This service's domain vocabulary (`Company`, `Branch`) was a bespoke naming that diverged from both the ThinkLab platform blueprint (which describes this capability as the "Tenant Service", modeling a Root/Branch hierarchy) and from the BIAN reference model, which defines an official Service Domain — **Party Reference Data Directory** — for exactly this kind of corporate master-data management. Keeping a custom name here would have meant inventing yet another vocabulary the moment a real BIAN-literate integration partner or the platform's own future services needed to reference this capability.

## Decision
Rename the Service Domain to `party-reference-data-directory` (route root `/party-reference-data-directory/v1`) and its Control Record from `Company` to `Organisation` (package/class-wide rename across domain, application, and infrastructure layers — `CompanyStatus` → `OrganisationStatus`, `CompanyRepository` → `OrganisationRepository`, `CompanyDocument` → `OrganisationDocument`, Mongo collection `companies` → `organisations`, all use cases and DTOs renamed to match). The physical `DELETE /{id}` endpoint is removed; the previously-unreachable `CompanyStatus.CANCELED` value is now a formal terminal `control/cancel` transition (see ADR-013), and the status state machine is now explicit (`OrganisationStatus.validateTransitionTo`), mirroring the Hash Token Registry's `HashStatus` pattern instead of the previous ad-hoc `if` check.

`Branch` is renamed to `OrganisationUnit` as part of this same change — see ADR-015 for why it also gains an independent lifecycle.
