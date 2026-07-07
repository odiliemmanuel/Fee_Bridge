#!/usr/bin/env python3
"""Generates the FeeBridge app-overview deck: docs/slides/FeeBridge-Overview.pptx
Run (from docs/slides):  ./.venv/bin/python generate_overview.py
"""
import os
from deck_common import new_presentation, title_slide, content_slide, save

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "FeeBridge-Overview.pptx")


def b(text, level=0):
    return (text, level)


def build():
    prs = new_presentation()

    title_slide(prs, "FeeBridge", "School Fee Payment & Reconciliation Platform — powered by Nomba")

    content_slide(prs, "The Problem", [
        b("Schools track fees on spreadsheets and paper receipts"),
        b("Parents pay by cash or ad-hoc bank transfer — hard to attribute"),
        b("Reconciling who paid what is slow and error-prone", 1),
        b("No single view of debtors, partial payments, or scholarships"),
        b("Manual notifications; little to no audit trail"),
    ])

    content_slide(prs, "The Solution — FeeBridge", [
        b("A multi-tenant platform where a school runs its entire fee lifecycle"),
        b("Define academic structure, classes and fees (day & boarding)"),
        b("Register students, parents/guardians and sponsors"),
        b("Collect fees online via Nomba, or record cash — all reconciled automatically"),
        b("Live analytics, filtered lists, downloads, notifications and a full audit log"),
    ])

    content_slide(prs, "Architecture", [
        b("React + TypeScript SPA (admin & parent portals)"),
        b("Spring Boot 3 (Java 21) modular backend, JWT-secured, multi-tenant"),
        b("PostgreSQL with Flyway migrations; money stored as exact integer kobo"),
        b("Nomba dynamic virtual accounts + HMAC-signed webhooks"),
        b("Email (SMTP) + pluggable SMS; CSV/Excel/PDF exports"),
        b("Docker Compose: postgres · mailhog · backend · frontend"),
    ])

    content_slide(prs, "Core Features", [
        b("Fees per class × session × term × residency, with change history"),
        b("Scholarships — percentage or fixed amount, per student"),
        b("Full / partial / over-payment; overpayment carries to next term"),
        b("Pay for multiple children in one transaction"),
        b("Filter by status (pending/partial/complete/overpayment) and class"),
        b("Student search; downloadable lists; analytics dashboard"),
        b("Offline cash payments with SMS + email alerts"),
    ])

    content_slide(prs, "The Nomba Payment Flow", [
        b("Parent selects children and amounts → backend creates a payment order"),
        b("A dedicated Nomba dynamic virtual account is minted for the order", 1),
        b("Parent transfers to that account number"),
        b("Nomba sends a signed webhook → HMAC verified, idempotent", 1),
        b("Payment matched to the order and applied across children"),
        b("Ledger verified, parent notified by SMS + email"),
    ])

    content_slide(prs, "Money & Reconciliation", [
        b("All amounts are exact integer minor units (kobo) — no floating point"),
        b("Append-only ledger per student is the source of truth"),
        b("Invariant: invoice balance == sum of its ledger entries", 1),
        b("'Verify with balance' runs after every payment"),
        b("Unmatched Nomba credits become reconciliation exceptions"),
        b("Scheduled reconciliation sweep + on-demand ledger verification"),
    ])

    content_slide(prs, "Security & Multi-tenancy", [
        b("Every record scoped by school_id; tenants fully isolated"),
        b("JWT bearer auth; role-based access (Admin, Bursar, Parent, Guardian)"),
        b("Webhook endpoint public but HMAC-verified and idempotent"),
        b("BCrypt passwords; input validation; secrets via environment"),
        b("Audit log records every state-changing action — who, what, when"),
    ])

    content_slide(prs, "Roles & Portals", [
        b("School Admin / Bursar: dashboard, students, fees, invoices, payments"),
        b("Reconciliation, downloads, notifications and audit", 1),
        b("Parent / Guardian: children & balances, multi-child pay, receipts"),
        b("Delegated sponsor / NGO can pay on a student's behalf"),
        b("Student: fee status and transaction history"),
    ])

    content_slide(prs, "Roadmap", [
        b("Nomba card checkout + scheduled transaction pull"),
        b("Payment plans, penalties, and fee line-items"),
        b("Scheduled reminders (SMS/email/WhatsApp) and a parent mobile app"),
        b("Refresh tokens, MFA, granular permissions"),
        b("Debtor-ageing and term-on-term trend analytics"),
        b("Platform-admin console and bulk CSV import"),
    ])

    content_slide(prs, "Demo", [
        b("docker compose up  → SPA on :5173, API on :8080"),
        b("School admin — admin@greenfield.edu.ng / password123"),
        b("Parent — obi@example.com / password123"),
        b("Thank you."),
    ])

    save(prs, OUT)


if __name__ == "__main__":
    build()
