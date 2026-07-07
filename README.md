# FeeBridge

**A multi-tenant school fee payment & reconciliation platform, powered by the Nomba API.**

Schools define their fees, register students and parents, and collect fees online (Nomba dynamic
virtual accounts + signed webhooks) or as cash — with automatic reconciliation, a full audit trail,
analytics, downloadable reports, and SMS/email notifications.

---

## Highlights

- 🏫 **Multi-tenant** — every school's data is fully isolated (`school_id` scoping + JWT tenancy).
- 💳 **Nomba payments** — each payment mints a dedicated virtual account; a **HMAC-signed webhook**
  settles and reconciles it idempotently.
- 👨‍👩‍👧‍👦 **Multi-child, one transfer** — a parent pays for several children in a single transaction.
- ➕ **Full / partial / over-payment** — overpayment is carried to the next term as credit.
- 🎓 **Scholarships** (percentage or fixed), **day/boarding** fees, **delegated sponsors/NGOs**.
- 🧾 **Offline cash payments** with automatic SMS + email alerts to parents.
- 🔎 Search & filter (status, class), 📥 CSV/Excel/PDF downloads, 📊 analytics dashboard.
- 🧮 **Exact money** (integer kobo) + append-only ledger; "verify with balance" reconciliation.
- 📝 **Audit log** of every state-changing action (who, what, when).

## Tech stack

Spring Boot 3.5 (Java 21) · PostgreSQL 16 / H2 · Flyway · Spring Security + JWT ·
React 18 + TypeScript + Vite + Recharts · Apache POI · OpenPDF · Docker Compose.

## Repository layout

```
FeeBridge/
├── backend/        Spring Boot API (package-modular monolith)
├── frontend/       React + TypeScript SPA (admin & parent portals)
├── docs/
│   ├── architecture.md
│   ├── FEATURES.md · FUTURE_RECOMMENDATIONS.md
│   ├── diagrams/   ERD + UML use-case + activity diagrams (.drawio) + generator
│   └── slides/     App-overview & training decks (.pptx) + python-pptx generators
└── docker-compose.yml
```

---

## Quick start

### Option A — Docker Compose (everything)

```bash
docker compose up --build
```

- Frontend  → http://localhost:5173
- API       → http://localhost:8080
- MailHog UI → http://localhost:8025  (see the emails FeeBridge sends)

### Option B — Run locally (no Docker)

The backend runs on in-memory **H2 in PostgreSQL mode** by default, so it needs nothing installed:

```bash
# Backend (http://localhost:8080) — seeds a demo school on first run
cd backend && ./gradlew bootRun

# Frontend (http://localhost:5173), proxies /api to :8080
cd frontend && npm install && npm run dev
```

### Demo logins (seeded automatically)

| Role         | Email                        | Password      |
|--------------|------------------------------|---------------|
| School Admin | `admin@greenfield.edu.ng`    | `password123` |
| Parent       | `obi@example.com`            | `password123` |

---

## Nomba configuration

Locally the app uses a **fake gateway** (`feebridge.nomba.enabled=false`) so the full payment +
webhook flow is demoable without Nomba. To use the real sandbox/production API, set:

```
NOMBA_ENABLED=true
NOMBA_BASE_URL=https://api.nomba.com
NOMBA_CLIENT_ID=...        NOMBA_CLIENT_SECRET=...
NOMBA_ACCOUNT_ID=...       NOMBA_SIGNATURE_KEY=...   # webhook HMAC key
```

Point your Nomba webhook at `POST /api/webhooks/nomba`.

## Try the payment flow end-to-end (fake gateway)

```bash
BASE=http://localhost:8080; KEY=sandbox-signature-key
TOKEN=$(curl -s -X POST $BASE/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"admin@greenfield.edu.ng","password":"password123"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
# create an order for student 1
ORDER=$(curl -s -X POST $BASE/api/payments/orders -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"allocations":[{"studentId":1,"amountNaira":30000}]}')
REF=$(echo "$ORDER" | python3 -c 'import sys,json;print(json.load(sys.stdin)["reference"])')
# simulate the signed Nomba webhook
BODY="{\"event_type\":\"payment_success\",\"data\":{\"transaction\":{\"reference\":\"NBTX-1\",\"amount\":30000.00,\"time\":\"2026-07-07T12:00:00Z\"},\"account\":{\"accountRef\":\"$REF\"}}}"
SIG=$(printf '%s' "$BODY" | openssl dgst -sha256 -hmac "$KEY" | awk '{print $NF}')
curl -s -X POST $BASE/api/webhooks/nomba -H 'Content-Type: application/json' -H "x-nomba-signature: $SIG" -d "$BODY"
```

## Key API endpoints

| Area          | Endpoint |
|---------------|----------|
| Auth          | `POST /api/auth/register-school`, `POST /api/auth/login`, `GET /api/auth/me` |
| Academics     | `POST/GET /api/sessions`, `POST /api/classes`, `POST/GET /api/fees`, `GET /api/fees/changes` |
| People        | `POST/GET /api/students` (search), `POST /api/students/{id}/guardians`, `POST /api/guardians/{id}/login` |
| Scholarships  | `POST /api/scholarships`, `POST /api/scholarships/awards` |
| Billing       | `POST /api/billing/assessments`, `GET /api/invoices` (filter), `GET /api/students/{id}/statement` |
| Payments      | `POST /api/payments/orders`, `POST /api/payments/offline`, `POST /api/webhooks/nomba` |
| Parent portal | `GET /api/parent/children`, `POST /api/parent/orders` |
| Reconciliation| `GET /api/reconciliation/exceptions`, `GET /api/reconciliation/verify-ledger` |
| Reports       | `GET /api/reports/invoices.{csv,xlsx,pdf}` |
| Analytics     | `GET /api/analytics/dashboard` |
| Audit         | `GET /api/audit` |

## Tests

```bash
cd backend && ./gradlew test
```

Covers the money value object, the billing engine (full/partial/over-payment, credit carry-over,
scholarships, ledger reconciliation) and the full Nomba online flow (signed webhook settlement,
idempotency, signature rejection).

## Documentation

- **Architecture** — [docs/architecture.md](docs/architecture.md)
- **All features** — [docs/FEATURES.md](docs/FEATURES.md)
- **Future work** — [docs/FUTURE_RECOMMENDATIONS.md](docs/FUTURE_RECOMMENDATIONS.md)
- **Diagrams** (ERD, use-case, activity) — `docs/diagrams/*.drawio` (open at [diagrams.net](https://app.diagrams.net));
  regenerate with `python3 docs/diagrams/generate_diagrams.py`
- **Slide decks** — `docs/slides/FeeBridge-Overview.pptx`, `FeeBridge-Training.pptx`;
  regenerate with `pip install -r docs/slides/requirements.txt && python docs/slides/generate_overview.py`
# Fee_Bridge
