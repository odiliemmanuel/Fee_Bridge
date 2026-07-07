# FeeBridge — Feature Catalogue

Every requirement from the brief mapped to what the platform delivers and where it lives.

## 1. School onboarding & tenancy
- A school self-registers, creating the school profile and its first **School Admin** account
  (`POST /api/auth/register-school`).
- Multi-tenant: all data is isolated per school (`school_id` scoping + `TenantContext`).
- School type is configurable: **day**, **boarding**, or both (`has_day`, `has_boarding`).
- Nomba account linkage stored on the school for routing collections.

## 2. Roles & access control
- Roles: `PLATFORM_ADMIN`, `SCHOOL_ADMIN`, `BURSAR`, `PARENT`, `GUARDIAN`.
- JWT auth; endpoints guarded by `@PreAuthorize`. Parents only ever see their own children.

## 3. Academic structure & fees
- **Sessions** (academic years); each session automatically has **three terms** (First/Second/Third).
- **Classes** per school; one current session & term at a time.
- **Fee structure** set per **class × session × term × residency** (day vs boarding).
- **Fee-change history**: every fee amount change is written to an immutable `fee_change_log`
  (old → new, who, when, reason) — "payment amount change for each term is kept".

## 4. Students
- Register students with admission number (auto-generated if blank), class, residency, photo, status.
- **Search** by name or admission number; **filter** by class and by status.
- Enrol a student into a session/class (history retained in `enrollments`).
- Update, graduate, or withdraw students.

## 5. Parents / guardians & mapping
- Register parents/guardians and **map children to them**.
- If a parent already exists (matched by phone within the school), the student is simply mapped —
  no duplicate is created.
- A student can have multiple guardians with roles: `FATHER`, `MOTHER`, `GUARDIAN`, `SPONSOR`, `NGO`.
- **Delegated payer / sponsor** — a guardian who is not the parent (sponsor/NGO) can be flagged
  `is_delegated_payer` and made responsible for a student's fees.
- Staff can provision a **parent-portal login** for any guardian.

## 6. Scholarships
- Percentage or fixed-amount scholarships; reusable templates or ad-hoc awards.
- Awards are **snapshotted** per student and can be scoped to a session/term or apply to all.
- Applied automatically during assessment, reducing the invoice net amount.

## 7. Billing engine
- **Assessment** generates a term invoice per active enrolment:
  `net = fee − scholarship − carried-in credit`. Idempotent per (student, term).
- Append-only **ledger** per student is the source of truth; invoice balances are derived and
  always reconcile to it.
- Invoice statuses: **PENDING**, **PARTIAL**, **PAID**, **OVERPAID** (the school's filters).

## 8. Payments — online (Nomba)
- **Each payment mints a dedicated Nomba dynamic virtual account** linked to the school, with an
  expected amount and expiry.
- **Full, partial, or over-payment** all supported.
- **Over-payment carries to the next term** as credit (student credit wallet, auto-applied).
- **Multi-child single transaction**: a parent selects several children, sets an amount for each,
  and pays once; the settlement is split across the children's invoices.
- **Signed webhook** settles payments: HMAC verification + idempotency (dedupe on transaction ref).

## 9. Payments — offline / cash
- Staff record cash / bank-transfer / POS payments against a student's invoice.
- The parent is **notified by SMS and email** on any offline payment.

## 10. Notifications
- Email (SMTP → MailHog locally) and SMS (pluggable: log / Termii / Twilio).
- Every attempt is persisted to `notification_logs` (SENT/FAILED) and viewable by the school.
- Payment receipts on successful online payment; cash-payment alerts on offline entry.

## 11. Reconciliation
- **Verify with balance**: after every payment the student's ledger is checked against invoice
  balances; a school-wide `verify-ledger` sweep is available.
- **Unmatched-credit detection**: a scheduled job (and on-demand run) flags Nomba credits that
  cannot be matched to an order as reconciliation exceptions for review.
- Exceptions can be resolved by staff.

## 12. Search, filter & lists
- Filter invoices by **payment status** (pending / partial / complete / overpayment) and by **class**.
- Search students; filter students by class and status.

## 13. Downloads / exports
- Invoice lists exportable as **CSV, Excel (.xlsx), and PDF**, honouring the class & status filters.

## 14. Analytics
- School dashboard: expected vs collected, collection rate, outstanding, scholarships, credit held,
  counts by status, breakdown by class, and **day-vs-boarding** split.

## 15. Dashboards
- **School admin/bursar**: analytics, students, invoices & payments, reconciliation.
- **Parent/guardian**: children with balances & credit, multi-child payment, Nomba account to pay to.
- **Student**: fee status and transaction history (via the student statement).

## 16. Transaction history & statements
- Per-student **statement**: all invoices, the full ledger, outstanding balance and credit balance.
- Per-school transaction log of Nomba settlements and offline payments.

## 17. Audit log
- **Every state-changing API call is audited** (who, what, when, target id, IP) via an AOP aspect,
  plus explicit business audit entries. "Upon successful payment, verify with balance" is enforced.

## 18. Day / boarding handling
- Fees, enrolment, invoices and analytics all distinguish **DAY** and **BOARDING** residency.
