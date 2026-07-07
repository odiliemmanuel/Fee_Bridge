# FeeBridge — Architecture

## Overview

FeeBridge is a **multi-tenant** SaaS platform for school fee collection and reconciliation.
Each school is a tenant; every record is scoped by `school_id` and every query is tenant-filtered
so no school can read or mutate another's data.

```
┌────────────────┐     HTTPS/JWT      ┌──────────────────────────────┐     JDBC     ┌────────────┐
│  React SPA     │  ───────────────▶  │   Spring Boot API (modular)  │ ───────────▶ │ PostgreSQL │
│  (admin/parent)│                    │                              │              └────────────┘
└────────────────┘                    │  auth · academics · people   │
                                       │  fee · scholarship · billing │   HMAC webhook  ┌────────────┐
        Nomba transfer ──────────────▶│  payments · nomba · recon    │ ◀────────────── │   Nomba    │
                                       │  notification · audit ·      │   REST (VA)  ──▶│  Payments  │
        SMS / SMTP  ◀──────────────────│  reporting · analytics       │                 └────────────┘
                                       └──────────────────────────────┘
```

## Stack

| Layer      | Technology |
|------------|------------|
| Backend    | Spring Boot 3.5 (Java 21), Spring Security + JWT, Spring Data JPA/Hibernate |
| Database   | PostgreSQL 16 (prod), H2 in PostgreSQL mode (local/dev/test); Flyway migrations |
| Frontend   | React 18 + TypeScript + Vite, React Router, Recharts, Axios |
| Payments   | Nomba dynamic virtual accounts + signed webhooks |
| Messaging  | SMTP email (MailHog locally), pluggable SMS (log / Termii / Twilio) |
| Reporting  | Apache POI (Excel), OpenPDF (PDF), CSV |
| Ops        | Docker Compose (postgres, mailhog, backend, frontend) |

## Backend module map (`com.feebridge.*`)

- **common** — `Money` value object (integer kobo, exact arithmetic), `BaseEntity`, error handling, `TenantContext`.
- **auth** — JWT issue/verify, RBAC (`PLATFORM_ADMIN`, `SCHOOL_ADMIN`, `BURSAR`, `PARENT`, `GUARDIAN`), school onboarding.
- **school** — tenant entity + Nomba account linkage, day/boarding flags.
- **academics** — sessions, three terms/session, classes.
- **fee** — fee structure per (class, session, term, residency) + immutable fee-change log.
- **people** — students, guardians, student↔guardian mapping (incl. delegated sponsor), enrolments, search.
- **scholarship** — percentage/fixed awards, snapshotted per student and scoped to session/term.
- **billing** — assessment (invoice generation), append-only **ledger**, credit wallet, the **payment waterfall**.
- **payments** — payment orders + allocations (multi-child), offline payments, transactions.
- **nomba** — OAuth2 client, dynamic virtual account creation, HMAC signature util, webhook controller.
- **reconciliation** — unmatched-credit scan (scheduled), ledger-vs-balance verification, exceptions.
- **notification** — email + SMS delivery with persisted `NotificationLog`.
- **audit** — AOP aspect recording every mutating API call + explicit `AuditLog`.
- **reporting** — CSV / Excel / PDF exports filtered by class & status.
- **analytics** — collections dashboard aggregates.

## Money & the ledger (the core invariant)

All amounts are **integer minor units (kobo)** behind the `Money` type — no floating point.
Each student has an append-only **ledger**; every charge/credit is one entry. The invariant

```
invoice.balance  ==  Σ(ledger entries for that invoice)
Σ(invoice balances for a student)  ==  Σ(all that student's ledger entries)
```

always holds, which is exactly what the reconciliation "verify with balance" check asserts.

**Payment waterfall:** a payment is capped at the invoice balance; any surplus flows to the
student's **credit wallet**, which is auto-applied to the next term's invoice (carry-over). This
yields the four invoice states the school filters on: `PENDING`, `PARTIAL`, `PAID`, `OVERPAID`.

## Nomba payment flow

1. Parent selects one or more children and amounts → backend creates a `PaymentOrder` with
   per-child `PaymentAllocation`s.
2. Backend mints a **dynamic virtual account** (`POST /v1/accounts/virtual`, `accountRef` = order
   reference, `expectedAmount` = total, `expiryDate` ≈ +30 min) linked to the school's Nomba account.
3. Parent transfers to that account. Nomba fires a **signed webhook**.
4. Backend verifies the HMAC signature, idempotently records the transaction (dedupe on reference),
   matches it by `accountRef`, runs the waterfall across allocations, verifies the ledger, and
   notifies the parent by SMS + email.
5. Unmatched credits and any ledger drift surface as reconciliation exceptions.

Locally (`feebridge.nomba.enabled=false`) a **fake gateway** mints deterministic accounts so the
entire flow — including the signed webhook — is demoable and testable without Nomba.

## Security & multi-tenancy

- Stateless JWT bearer auth; method-level `@PreAuthorize` RBAC.
- `TenantContext` (from JWT claims) + `school_id`-scoped repository queries isolate tenants.
- Webhook endpoint is public but **HMAC-verified** and idempotent.
- BCrypt password hashing; bean-validation on all inputs; secrets via environment config.
