[//]: # (# FeeBridge)

[//]: # ()
[//]: # (**A multi-tenant school fee payment & reconciliation platform, powered by the Nomba API.**)

[//]: # ()
[//]: # (Schools define their fees, register students and parents, and collect fees online &#40;Nomba dynamic)

[//]: # (virtual accounts + signed webhooks&#41; or as cash — with automatic reconciliation, a full audit trail,)

[//]: # (analytics, downloadable reports, and SMS/email notifications.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Highlights)

[//]: # ()
[//]: # (- 🏫 **Multi-tenant** — every school's data is fully isolated &#40;`school_id` scoping + JWT tenancy&#41;.)

[//]: # (- 💳 **Nomba payments** — each payment mints a dedicated virtual account; a **HMAC-signed webhook**)

[//]: # (  settles and reconciles it idempotently.)

[//]: # (- 👨‍👩‍👧‍👦 **Multi-child, one transfer** — a parent pays for several children in a single transaction.)

[//]: # (- ➕ **Full / partial / over-payment** — overpayment is carried to the next term as credit.)

[//]: # (- 🎓 **Scholarships** &#40;percentage or fixed&#41;, **day/boarding** fees, **delegated sponsors/NGOs**.)

[//]: # (- 🧾 **Offline cash payments** with automatic SMS + email alerts to parents.)

[//]: # (- 🔎 Search & filter &#40;status, class&#41;, 📥 CSV/Excel/PDF downloads, 📊 analytics dashboard.)

[//]: # (- 🧮 **Exact money** &#40;integer kobo&#41; + append-only ledger; "verify with balance" reconciliation.)

[//]: # (- 📝 **Audit log** of every state-changing action &#40;who, what, when&#41;.)

[//]: # ()
[//]: # (## Tech stack)

[//]: # ()
[//]: # (Spring Boot 3.5 &#40;Java 21&#41; · PostgreSQL 16 / H2 · Flyway · Spring Security + JWT ·)

[//]: # (React 18 + TypeScript + Vite + Recharts · Apache POI · OpenPDF · Docker Compose.)

[//]: # ()
[//]: # (## Repository layout)

[//]: # ()
[//]: # (```)

[//]: # (FeeBridge/)

[//]: # (├── backend/        Spring Boot API &#40;package-modular monolith&#41;)

[//]: # (├── frontend/       React + TypeScript SPA &#40;admin & parent portals&#41;)

[//]: # (├── docs/)

[//]: # (│   ├── architecture.md)

[//]: # (│   ├── FEATURES.md · FUTURE_RECOMMENDATIONS.md)

[//]: # (│   ├── diagrams/   ERD + UML use-case + activity diagrams &#40;.drawio&#41; + generator)

[//]: # (│   └── slides/     App-overview & training decks &#40;.pptx&#41; + python-pptx generators)

[//]: # (└── docker-compose.yml)

[//]: # (```)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Quick start)

[//]: # ()
[//]: # (### Option A — Docker Compose &#40;everything&#41;)

[//]: # ()
[//]: # (```bash)

[//]: # (docker compose up --build)

[//]: # (```)

[//]: # ()
[//]: # (- Frontend  → http://localhost:5173)

[//]: # (- API       → http://localhost:8080)

[//]: # (- MailHog UI → http://localhost:8025  &#40;see the emails FeeBridge sends&#41;)

[//]: # ()
[//]: # (### Option B — Run locally &#40;no Docker&#41;)

[//]: # ()
[//]: # (The backend runs on in-memory **H2 in PostgreSQL mode** by default, so it needs nothing installed:)

[//]: # ()
[//]: # (```bash)

[//]: # (# Backend &#40;http://localhost:8080&#41; — seeds a demo school on first run)

[//]: # (cd backend && ./gradlew bootRun)

[//]: # ()
[//]: # (# Frontend &#40;http://localhost:5173&#41;, proxies /api to :8080)

[//]: # (cd frontend && npm install && npm run dev)

[//]: # (```)

[//]: # ()
[//]: # (### Demo logins &#40;seeded automatically&#41;)

[//]: # ()
[//]: # (| Role         | Email                        | Password      |)

[//]: # (|--------------|------------------------------|---------------|)

[//]: # (| School Admin | `admin@greenfield.edu.ng`    | `password123` |)

[//]: # (| Parent       | `obi@example.com`            | `password123` |)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Nomba configuration)

[//]: # ()
[//]: # (Locally the app uses a **fake gateway** &#40;`feebridge.nomba.enabled=false`&#41; so the full payment +)

[//]: # (webhook flow is demoable without Nomba. To use the real sandbox/production API, set:)

[//]: # ()
[//]: # (```)

[//]: # (NOMBA_ENABLED=true)

[//]: # (NOMBA_BASE_URL=https://api.nomba.com)

[//]: # (NOMBA_CLIENT_ID=...        NOMBA_CLIENT_SECRET=...)

[//]: # (NOMBA_ACCOUNT_ID=...       NOMBA_SIGNATURE_KEY=...   # webhook HMAC key)

[//]: # (```)

[//]: # ()
[//]: # (Point your Nomba webhook at `POST /api/webhooks/nomba`.)

[//]: # ()
[//]: # (## Try the payment flow end-to-end &#40;fake gateway&#41;)

[//]: # ()
[//]: # (```bash)

[//]: # (BASE=http://localhost:8080; KEY=sandbox-signature-key)

[//]: # (TOKEN=$&#40;curl -s -X POST $BASE/api/auth/login -H 'Content-Type: application/json' \)

[//]: # (  -d '{"email":"admin@greenfield.edu.ng","password":"password123"}' | python3 -c 'import sys,json;print&#40;json.load&#40;sys.stdin&#41;["token"]&#41;'&#41;)

[//]: # (# create an order for student 1)

[//]: # (ORDER=$&#40;curl -s -X POST $BASE/api/payments/orders -H "Authorization: Bearer $TOKEN" \)

[//]: # (  -H 'Content-Type: application/json' -d '{"allocations":[{"studentId":1,"amountNaira":30000}]}'&#41;)

[//]: # (REF=$&#40;echo "$ORDER" | python3 -c 'import sys,json;print&#40;json.load&#40;sys.stdin&#41;["reference"]&#41;'&#41;)

[//]: # (# simulate the signed Nomba webhook)

[//]: # (BODY="{\"event_type\":\"payment_success\",\"data\":{\"transaction\":{\"reference\":\"NBTX-1\",\"amount\":30000.00,\"time\":\"2026-07-07T12:00:00Z\"},\"account\":{\"accountRef\":\"$REF\"}}}")

[//]: # (SIG=$&#40;printf '%s' "$BODY" | openssl dgst -sha256 -hmac "$KEY" | awk '{print $NF}'&#41;)

[//]: # (curl -s -X POST $BASE/api/webhooks/nomba -H 'Content-Type: application/json' -H "x-nomba-signature: $SIG" -d "$BODY")

[//]: # (```)

[//]: # ()
[//]: # (## Key API endpoints)

[//]: # ()
[//]: # (| Area          | Endpoint |)

[//]: # (|---------------|----------|)

[//]: # (| Auth          | `POST /api/auth/register-school`, `POST /api/auth/login`, `GET /api/auth/me` |)

[//]: # (| Academics     | `POST/GET /api/sessions`, `POST /api/classes`, `POST/GET /api/fees`, `GET /api/fees/changes` |)

[//]: # (| People        | `POST/GET /api/students` &#40;search&#41;, `POST /api/students/{id}/guardians`, `POST /api/guardians/{id}/login` |)

[//]: # (| Scholarships  | `POST /api/scholarships`, `POST /api/scholarships/awards` |)

[//]: # (| Billing       | `POST /api/billing/assessments`, `GET /api/invoices` &#40;filter&#41;, `GET /api/students/{id}/statement` |)

[//]: # (| Payments      | `POST /api/payments/orders`, `POST /api/payments/offline`, `POST /api/webhooks/nomba` |)

[//]: # (| Parent portal | `GET /api/parent/children`, `POST /api/parent/orders` |)

[//]: # (| Reconciliation| `GET /api/reconciliation/exceptions`, `GET /api/reconciliation/verify-ledger` |)

[//]: # (| Reports       | `GET /api/reports/invoices.{csv,xlsx,pdf}` |)

[//]: # (| Analytics     | `GET /api/analytics/dashboard` |)

[//]: # (| Audit         | `GET /api/audit` |)

[//]: # ()
[//]: # (## Tests)

[//]: # ()
[//]: # (```bash)

[//]: # (cd backend && ./gradlew test)

[//]: # (```)

[//]: # ()
[//]: # (Covers the money value object, the billing engine &#40;full/partial/over-payment, credit carry-over,)

[//]: # (scholarships, ledger reconciliation&#41; and the full Nomba online flow &#40;signed webhook settlement,)

[//]: # (idempotency, signature rejection&#41;.)

[//]: # ()
[//]: # (## Documentation)

[//]: # ()
[//]: # (- **Architecture** — [docs/architecture.md]&#40;docs/architecture.md&#41;)

[//]: # (- **All features** — [docs/FEATURES.md]&#40;docs/FEATURES.md&#41;)

[//]: # (- **Future work** — [docs/FUTURE_RECOMMENDATIONS.md]&#40;docs/FUTURE_RECOMMENDATIONS.md&#41;)

[//]: # (- **Diagrams** &#40;ERD, use-case, activity&#41; — `docs/diagrams/*.drawio` &#40;open at [diagrams.net]&#40;https://app.diagrams.net&#41;&#41;;)

[//]: # (  regenerate with `python3 docs/diagrams/generate_diagrams.py`)

[//]: # (- **Slide decks** — `docs/slides/FeeBridge-Overview.pptx`, `FeeBridge-Training.pptx`;)

[//]: # (  regenerate with `pip install -r docs/slides/requirements.txt && python docs/slides/generate_overview.py`)

[//]: # (# Fee_Bridge)

[//]: # (<<<<<<< HEAD)

[//]: # (# Fee_Bridge)

[//]: # (=======)

[//]: # (>>>>>>> d98730b2107f515086f2641009a6aa40eeb5cb38)


    # Fee_Bridge
    