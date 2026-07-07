# FeeBridge — Future Recommendations

Prioritised enhancements beyond the current build.

## Payments & finance
- **Nomba card checkout** in addition to virtual-account transfers, for instant confirmation.
- **Automatic Nomba transaction pull** (scheduled `listTransactions`) to reconcile even if a webhook
  is missed, and to auto-resolve unmatched credits by amount + time window.
- **Payment plans / instalments**: schedule expected part-payments with reminders.
- **Refunds & credit payouts**: return a student's credit wallet to the payer on withdrawal.
- **Settlement reports** per school with Nomba fees broken out.

## Fees & billing
- **Fee components** (tuition, PTA, uniform, exam) as line items on an invoice rather than a single fee.
- **Late-payment penalties** and early-payment discounts.
- **Bulk fee editing** and copy-fees-from-previous-session.
- **Invoice re-assessment** when a fee or scholarship changes after invoices are issued.

## Notifications & engagement
- **Scheduled fee reminders** (T-7, due date, overdue) by SMS/email/WhatsApp.
- **Templated, brandable messages** per school; delivery-status callbacks from the SMS provider.
- **Parent mobile app** (React Native) reusing the same API.

## Access & security
- **Refresh tokens + token revocation**; optional **MFA** for admins/bursars.
- **Granular permissions** (e.g. bursar can record payments but not edit fees).
- **Row-level security** in Postgres as defence-in-depth for tenancy.
- **PII encryption at rest** for guardian contact details.

## Reporting & analytics
- **Trends over time** (collections per week, term-on-term) and **debtor ageing** reports.
- **Scheduled report delivery** (email the bursar a weekly collections PDF).
- **Cohort/scholarship analytics** (funded vs paying students, sponsor impact).

## Platform & operations
- **Platform-admin console** to onboard/suspend schools and monitor cross-tenant reconciliation.
- **Bulk student/guardian CSV import** with validation and dry-run.
- **Observability**: metrics (Micrometer/Prometheus), structured logs, tracing on the payment path.
- **Testcontainers CI** against real PostgreSQL, plus WireMock contract tests for the Nomba API.
- **Idempotency keys** on order creation to guard against double submits from the SPA.

## Domain extensions
- **Multi-currency** support for international schools.
- **Sibling/family discounts** applied automatically.
- **Special-needs / accessibility flags** on students with tailored fee handling.
- **Report cards / results** module to make FeeBridge a broader school-management suite.
