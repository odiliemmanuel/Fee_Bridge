#!/usr/bin/env python3
"""
Generates the FeeBridge design diagrams as draw.io (.drawio) files:
  - erd.drawio                     Entity-Relationship Diagram
  - use-case.drawio                UML Use-Case Diagram
  - activity-online-payment.drawio UML Activity Diagram (Nomba online payment + reconciliation)
  - activity-offline-payment.drawio UML Activity Diagram (offline/cash payment)

Pure standard library — no dependencies. Open the output in https://app.diagrams.net.
Run:  python3 generate_diagrams.py
"""
import html
import os

OUT = os.path.dirname(os.path.abspath(__file__))


def esc(s: str) -> str:
    return html.escape(s, quote=True)


def vertex(cid, value, x, y, w, h, style):
    return (f'<mxCell id="{cid}" value="{esc(value)}" style="{style}" vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')


def edge(cid, source, target, label="", style="edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=block;"):
    return (f'<mxCell id="{cid}" value="{esc(label)}" style="{style}" edge="1" parent="1" '
            f'source="{source}" target="{target}"><mxGeometry relative="1" as="geometry"/></mxCell>')


def document(name, cells):
    body = "".join(cells)
    return (f'<mxfile host="feebridge">'
            f'<diagram id="{name}" name="{name}">'
            f'<mxGraphModel dx="1400" dy="900" grid="1" gridSize="10" guides="1" '
            f'tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" '
            f'pageWidth="1600" pageHeight="1100" math="0" shadow="0">'
            f'<root><mxCell id="0"/><mxCell id="1" parent="0"/>{body}</root>'
            f'</mxGraphModel></diagram></mxfile>')


def write(filename, xml):
    path = os.path.join(OUT, filename)
    with open(path, "w", encoding="utf-8") as f:
        f.write(xml)
    print("wrote", path)


# --------------------------------------------------------------------------- ERD
ENTITIES = {
    "School": ["id (PK)", "name", "code", "currency", "has_day", "has_boarding", "nomba_account_id", "status"],
    "User": ["id (PK)", "school_id (FK)", "email", "password_hash", "status"],
    "Role": ["id (PK)", "name"],
    "UserRole": ["user_id (FK)", "role_id (FK)"],
    "AcademicSession": ["id (PK)", "school_id (FK)", "name", "is_current"],
    "Term": ["id (PK)", "session_id (FK)", "name", "sequence", "is_current"],
    "SchoolClass": ["id (PK)", "school_id (FK)", "name", "level_order"],
    "FeeStructure": ["id (PK)", "class_id (FK)", "session_id (FK)", "term_id (FK)", "residency_type", "amount_kobo"],
    "FeeChangeLog": ["id (PK)", "fee_structure_id (FK)", "old_amount_kobo", "new_amount_kobo", "changed_by_user_id", "reason"],
    "Student": ["id (PK)", "school_id (FK)", "admission_no", "class_id (FK)", "residency_type", "status"],
    "Guardian": ["id (PK)", "school_id (FK)", "user_id (FK)", "phone", "email"],
    "StudentGuardian": ["id (PK)", "student_id (FK)", "guardian_id (FK)", "relationship", "is_payer", "is_delegated_payer"],
    "Enrollment": ["id (PK)", "student_id (FK)", "session_id (FK)", "class_id (FK)", "residency_type"],
    "Scholarship": ["id (PK)", "school_id (FK)", "type", "percentage", "amount_kobo"],
    "StudentScholarship": ["id (PK)", "student_id (FK)", "scholarship_id (FK)", "session_id", "term_id", "status"],
    "Invoice": ["id (PK)", "student_id (FK)", "session_id (FK)", "term_id (FK)", "gross", "scholarship", "credit_applied", "net", "paid", "balance", "status"],
    "LedgerEntry": ["id (PK)", "student_id (FK)", "invoice_id (FK)", "entry_type", "amount_kobo (signed)"],
    "StudentCredit": ["id (PK)", "student_id (FK)", "balance_kobo"],
    "PaymentOrder": ["id (PK)", "school_id (FK)", "reference", "payer_guardian_id (FK)", "total_amount_kobo", "status"],
    "PaymentAllocation": ["id (PK)", "order_id (FK)", "student_id (FK)", "invoice_id (FK)", "amount_kobo", "applied"],
    "VirtualAccount": ["id (PK)", "order_id (FK)", "account_ref", "account_number", "expected_amount_kobo", "expiry_at"],
    "PaymentTransaction": ["id (PK)", "order_id (FK)", "provider_reference", "amount_kobo", "status", "matched"],
    "OfflinePayment": ["id (PK)", "student_id (FK)", "invoice_id (FK)", "method", "amount_kobo", "recorded_by_user_id"],
    "ReconciliationException": ["id (PK)", "school_id", "transaction_id (FK)", "type", "resolved"],
    "NotificationLog": ["id (PK)", "school_id", "channel", "recipient", "status"],
    "AuditLog": ["id (PK)", "school_id", "actor_user_id", "action", "entity_type", "entity_id"],
}

RELATIONSHIPS = [
    ("School", "User", "1..*"), ("School", "SchoolClass", "1..*"), ("School", "AcademicSession", "1..*"),
    ("School", "Student", "1..*"), ("School", "Guardian", "1..*"), ("School", "Scholarship", "1..*"),
    ("School", "PaymentOrder", "1..*"),
    ("User", "UserRole", "1..*"), ("Role", "UserRole", "1..*"),
    ("AcademicSession", "Term", "1..3"), ("SchoolClass", "FeeStructure", "1..*"),
    ("Term", "FeeStructure", "1..*"), ("FeeStructure", "FeeChangeLog", "1..*"),
    ("Student", "StudentGuardian", "1..*"), ("Guardian", "StudentGuardian", "1..*"),
    ("Student", "Enrollment", "1..*"), ("Student", "Invoice", "1..*"),
    ("Student", "LedgerEntry", "1..*"), ("Student", "StudentCredit", "1..1"),
    ("Student", "StudentScholarship", "1..*"), ("Scholarship", "StudentScholarship", "1..*"),
    ("Invoice", "LedgerEntry", "1..*"),
    ("PaymentOrder", "PaymentAllocation", "1..*"), ("PaymentOrder", "VirtualAccount", "1..1"),
    ("PaymentOrder", "PaymentTransaction", "1..*"), ("Guardian", "PaymentOrder", "0..*"),
    ("Student", "PaymentAllocation", "1..*"), ("Student", "OfflinePayment", "1..*"),
    ("PaymentTransaction", "ReconciliationException", "0..*"),
]


def build_erd():
    cells = []
    ids = {}
    cols, col_w, gap_x, gap_y = 5, 240, 40, 24
    y_cursor = [40] * cols
    header = ("rounded=0;whiteSpace=wrap;html=1;align=left;verticalAlign=top;spacingLeft=6;"
              "fillColor=#f5f8ff;strokeColor=#3b5bdb;")
    for i, (name, fields) in enumerate(ENTITIES.items()):
        col = i % cols
        x = 40 + col * (col_w + gap_x)
        y = y_cursor[col]
        h = 28 + 16 * len(fields)
        value = "<b>" + name + "</b><br>" + "<br>".join(fields)
        cid = f"e{i}"
        ids[name] = cid
        cells.append(vertex(cid, value, x, y, col_w, h, header))
        y_cursor[col] = y + h + gap_y
    for j, (a, b, card) in enumerate(RELATIONSHIPS):
        cells.append(edge(f"r{j}", ids[a], ids[b], card,
                          "edgeStyle=entityRelationEdgeStyle;fontSize=10;html=1;rounded=1;"
                          "endArrow=ERmany;startArrow=ERone;strokeColor=#94a3b8;"))
    return document("ERD", cells)


# ---------------------------------------------------------------------- Use-case
def build_use_case():
    cells = []
    actor_style = "shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;"
    uc_style = "ellipse;whiteSpace=wrap;html=1;fillColor=#e7f0ff;strokeColor=#3b5bdb;"
    ext_style = "shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;fillColor=#ffe8cc;"

    # System boundary
    cells.append(vertex("bnd", "FeeBridge Platform", 300, 20, 760, 1040,
                        "rounded=0;html=1;verticalAlign=top;fillColor=none;strokeColor=#748ffc;dashed=1;"
                        "fontStyle=1;spacingTop=6;"))

    actors_left = [("School Admin", 60, 120), ("Bursar", 60, 320), ("Parent", 60, 560), ("Guardian / Sponsor", 60, 760)]
    for i, (label, x, y) in enumerate(actors_left):
        cells.append(vertex(f"al{i}", label, x, y, 40, 80, actor_style))
    cells.append(vertex("stu", "Student", 60, 940, 40, 80, actor_style))
    cells.append(vertex("nomba", "Nomba (external)", 1120, 300, 40, 80, ext_style))
    cells.append(vertex("sms", "SMS / Email (external)", 1120, 640, 40, 80, ext_style))

    use_cases = [
        ("u0", "Onboard school", 380, 70),
        ("u1", "Manage sessions, terms, classes", 360, 150),
        ("u2", "Set / change fees (history kept)", 360, 230),
        ("u3", "Register students", 360, 310),
        ("u4", "Register & map guardians", 360, 390),
        ("u5", "Assign delegated sponsor", 660, 390),
        ("u6", "Award scholarships", 660, 230),
        ("u7", "Generate term invoices", 660, 150),
        ("u8", "Record offline payment", 360, 470),
        ("u9", "View analytics dashboard", 360, 900),
        ("u10", "Search / filter students & invoices", 360, 820),
        ("u11", "Download lists (CSV/Excel/PDF)", 660, 820),
        ("u12", "Review reconciliation", 660, 900),
        ("u13", "View audit log", 660, 980),
        ("u14", "View children & balances", 380, 560),
        ("u15", "Pay one/many children", 380, 640),
        ("u16", "Receive Nomba account", 680, 640),
        ("u17", "Settle payment (webhook)", 680, 560),
        ("u18", "Get payment notification", 680, 720),
        ("u19", "View fee status & history", 380, 950),
    ]
    for cid, label, x, y in use_cases:
        cells.append(vertex(cid, label, x, y, 200, 60, uc_style))

    links = [
        ("al0", ["u0", "u1", "u2", "u3", "u4", "u5", "u6", "u7", "u9", "u10", "u11", "u12", "u13"]),
        ("al1", ["u2", "u3", "u7", "u8", "u10", "u11", "u12"]),
        ("al2", ["u14", "u15"]),
        ("al3", ["u14", "u15"]),
        ("stu", ["u19"]),
    ]
    n = 0
    for actor, ucs in links:
        for uc in ucs:
            cells.append(edge(f"lk{n}", actor, uc, "", "endArrow=none;html=1;strokeColor=#adb5bd;"))
            n += 1
    cells.append(edge("lkn1", "u15", "nomba", "«uses»", "endArrow=open;dashed=1;html=1;"))
    cells.append(edge("lkn2", "u17", "nomba", "«webhook»", "endArrow=open;dashed=1;html=1;"))
    cells.append(edge("lkn3", "u18", "sms", "«uses»", "endArrow=open;dashed=1;html=1;"))
    cells.append(edge("lkn4", "u8", "u18", "«include»", "endArrow=open;dashed=1;html=1;"))
    return document("UseCase", cells)


# --------------------------------------------------------------- Activity: online
def act_start(cid, x, y):
    return vertex(cid, "", x, y, 30, 30, "ellipse;html=1;fillColor=#333;strokeColor=#333;")


def act_end(cid, x, y):
    return vertex(cid, "", x, y, 30, 30, "ellipse;html=1;fillColor=none;strokeColor=#333;"
                                          "shape=endState;")


def act_action(cid, label, x, y, w=220, h=50):
    return vertex(cid, label, x, y, w, h, "rounded=1;whiteSpace=wrap;html=1;arcSize=40;"
                                          "fillColor=#e7f0ff;strokeColor=#3b5bdb;")


def act_decision(cid, label, x, y):
    return vertex(cid, label, x, y, 150, 90, "rhombus;whiteSpace=wrap;html=1;fillColor=#fff3bf;strokeColor=#f0a500;")


def build_activity_online():
    c = []
    c.append(act_start("s", 130, 20))
    c.append(act_action("a1", "Parent selects children & amounts", 40, 80))
    c.append(act_action("a2", "Backend creates PaymentOrder + allocations", 40, 160))
    c.append(act_action("a3", "Mint Nomba dynamic virtual account", 40, 240))
    c.append(act_action("a4", "Show account number to parent", 40, 320))
    c.append(act_action("a5", "Parent transfers to account", 40, 400))
    c.append(act_action("a6", "Nomba sends signed webhook", 40, 480))
    c.append(act_decision("d1", "Valid HMAC signature?", 75, 560))
    c.append(act_action("rej", "Reject (401) — no change", 340, 575, 180, 50))
    c.append(act_decision("d2", "Duplicate transaction ref?", 75, 690))
    c.append(act_action("ig", "Ignore (idempotent)", 340, 705, 180, 50))
    c.append(act_action("a7", "Match by accountRef; run payment waterfall", 40, 810))
    c.append(act_action("a8", "Update invoices, ledger & credit wallet", 40, 890))
    c.append(act_action("a9", "Verify ledger == balance (reconcile)", 40, 970))
    c.append(act_action("a10", "Notify parent by SMS + email", 40, 1050))
    c.append(act_end("e", 135, 1140))

    seq = [("s", "a1"), ("a1", "a2"), ("a2", "a3"), ("a3", "a4"), ("a4", "a5"),
           ("a5", "a6"), ("a6", "d1")]
    for i, (a, b) in enumerate(seq):
        c.append(edge(f"o{i}", a, b))
    c.append(edge("od1n", "d1", "rej", "no"))
    c.append(edge("od1y", "d1", "d2", "yes"))
    c.append(edge("od2y", "d2", "ig", "yes"))
    c.append(edge("od2n", "d2", "a7", "no"))
    for i, (a, b) in enumerate([("a7", "a8"), ("a8", "a9"), ("a9", "a10"), ("a10", "e")]):
        c.append(edge(f"p{i}", a, b))
    return document("ActivityOnlinePayment", c)


# -------------------------------------------------------------- Activity: offline
def build_activity_offline():
    c = []
    c.append(act_start("s", 130, 20))
    c.append(act_action("a1", "Parent pays cash / transfer / POS at school", 40, 80))
    c.append(act_action("a2", "Bursar opens student invoice", 40, 160))
    c.append(act_action("a3", "Record offline payment (amount, method)", 40, 240))
    c.append(act_decision("d1", "Amount &gt; balance?", 75, 320))
    c.append(act_action("a4", "Apply to balance; surplus to credit wallet", 40, 450, 220, 50))
    c.append(act_action("a5", "Apply full amount to invoice", 320, 335, 200, 50))
    c.append(act_action("a6", "Write ledger entry; update status", 40, 540))
    c.append(act_action("a7", "Verify ledger == balance", 40, 620))
    c.append(act_action("a8", "Send SMS + email alert to parent", 40, 700))
    c.append(act_end("e", 135, 790))

    for i, (a, b) in enumerate([("s", "a1"), ("a1", "a2"), ("a2", "a3"), ("a3", "d1")]):
        c.append(edge(f"o{i}", a, b))
    c.append(edge("d1y", "d1", "a4", "yes"))
    c.append(edge("d1n", "d1", "a5", "no"))
    c.append(edge("m1", "a5", "a6"))
    c.append(edge("m2", "a4", "a6"))
    for i, (a, b) in enumerate([("a6", "a7"), ("a7", "a8"), ("a8", "e")]):
        c.append(edge(f"p{i}", a, b))
    return document("ActivityOfflinePayment", c)


if __name__ == "__main__":
    write("erd.drawio", build_erd())
    write("use-case.drawio", build_use_case())
    write("activity-online-payment.drawio", build_activity_online())
    write("activity-offline-payment.drawio", build_activity_offline())
    print("Done. Open the .drawio files at https://app.diagrams.net")
