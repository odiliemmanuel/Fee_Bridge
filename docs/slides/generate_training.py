#!/usr/bin/env python3
"""Generates the FeeBridge training deck: docs/slides/FeeBridge-Training.pptx
Run (from docs/slides):  ./.venv/bin/python generate_training.py
"""
import os
from deck_common import new_presentation, title_slide, content_slide, save

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "FeeBridge-Training.pptx")


def b(text, level=0):
    return (text, level)


def build():
    prs = new_presentation()

    title_slide(prs, "FeeBridge — Training Guide", "How to run your school's fees on FeeBridge")

    content_slide(prs, "Who does what", [
        b("School Admin — full setup: academics, fees, students, staff, settings"),
        b("Bursar — day-to-day payments, invoices, reports, reconciliation"),
        b("Parent / Guardian — view children, pay fees, see receipts"),
        b("Sponsor / NGO — a delegated payer assigned to specific students"),
    ])

    content_slide(prs, "Step 1 — Onboard your school", [
        b("Go to Sign up and create your school and admin account"),
        b("Set whether you run Day, Boarding, or both"),
        b("You are taken to the admin dashboard"),
    ], subtitle="Admin")

    content_slide(prs, "Step 2 — Set up academics", [
        b("Create your Classes (e.g. JSS1, JSS2, SS1)"),
        b("Create a Session (e.g. 2024/2025) — three terms are created automatically"),
        b("Activate the current term"),
    ], subtitle="Admin")

    content_slide(prs, "Step 3 — Set fees", [
        b("For each class, set the fee for each term"),
        b("Set separate amounts for Day and Boarding students", 1),
        b("Every fee change is recorded with who/when/old→new/reason"),
    ], subtitle="Admin")

    content_slide(prs, "Step 4 — Register students", [
        b("Add a student: name, class, residency (day/boarding)"),
        b("Admission number is generated automatically if left blank"),
        b("Auto-enrol into the current session"),
        b("Search and filter students by class or status any time"),
    ], subtitle="Admin / Bursar")

    content_slide(prs, "Step 5 — Register & map parents", [
        b("Add a guardian while creating a student, or separately"),
        b("If the parent already exists (same phone), the child is simply mapped"),
        b("Assign a Sponsor or NGO as a delegated payer where needed"),
        b("Provision a parent-portal login for any guardian"),
    ], subtitle="Admin / Bursar")

    content_slide(prs, "Step 6 — Scholarships", [
        b("Create a scholarship (percentage or fixed amount)"),
        b("Award it to a student; scope it to a session/term or leave open"),
        b("It is applied automatically when invoices are generated"),
    ], subtitle="Admin")

    content_slide(prs, "Step 7 — Generate term invoices", [
        b("Run assessment for the session and term"),
        b("An invoice is created for every active student with a fee"),
        b("Scholarships and any carried-over credit are applied automatically"),
    ], subtitle="Admin / Bursar")

    content_slide(prs, "Step 8 — Take an online payment", [
        b("Parent (or bursar) selects children and enters an amount for each"),
        b("Click Pay — a Nomba account number is shown"),
        b("Parent transfers to it; FeeBridge settles and reconciles automatically"),
        b("Full, partial and over-payment are all supported"),
    ], subtitle="Parent / Bursar")

    content_slide(prs, "Step 9 — Record a cash payment", [
        b("Open the student's invoice → Record cash"),
        b("Enter amount and method (cash / transfer / POS)"),
        b("The parent is alerted by SMS and email automatically"),
    ], subtitle="Bursar")

    content_slide(prs, "Step 10 — Filter, search & download", [
        b("Filter invoices by status: pending, partial, complete, overpayment"),
        b("Filter by class; search students by name or admission number"),
        b("Download the current list as CSV, Excel or PDF"),
    ], subtitle="Admin / Bursar")

    content_slide(prs, "Step 11 — Analytics & reconciliation", [
        b("Dashboard: expected vs collected, outstanding, collection rate"),
        b("Breakdowns by class and by day/boarding"),
        b("Reconciliation: verify ledgers and review any unmatched credits"),
        b("Audit log shows who changed what and when"),
    ], subtitle="Admin / Bursar")

    content_slide(prs, "Parent portal walkthrough", [
        b("Sign in with your parent login"),
        b("See each child's outstanding balance and any credit"),
        b("Enter an amount per child and pay once"),
        b("Copy the Nomba account, transfer, and you're done"),
    ], subtitle="Parent")

    content_slide(prs, "Support", [
        b("Demo admin — admin@greenfield.edu.ng / password123"),
        b("Demo parent — obi@example.com / password123"),
        b("Questions? Reach the FeeBridge team."),
    ])

    save(prs, OUT)


if __name__ == "__main__":
    build()
