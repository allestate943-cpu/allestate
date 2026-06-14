Checklist
- Provide architecture diagram (PlantUML + ASCII)
- Explain components and responsibilities
- Show data-flow sequences for key flows
- Add instructions to render PlantUML locally

Overview
--------
This repository implements a modular Spring Boot monolith for a real-estate + local services marketplace. The diagram in `architecture.puml` models both the production AWS architecture and the local developer experience (docker-compose). Use the PlantUML file to render a visual diagram.

ASCII Component Diagram (quick)

User/Client
   |
   v
ALB (HTTPS)
   |
   v
Spring Boot App (ECS Tasks / local container)
   |- ListingController (REST)
   |- ListingService (business logic)
   |- ListingRepository (JPA -> Postgres)
   |- Integrations: Anthropic, S3, OpenSearch, SQS
   v
Postgres (RDS in prod / container in dev)

Key components and responsibilities
- Spring Boot App: REST API, service layer, background workers, Flyway migration at startup.
- RDS / Postgres: persistent storage for entities (listings, users, providers, leads).
- S3: image & static asset storage using presigned URLs for direct client uploads.
- OpenSearch: full-text and geo search (can be replaced by Postgres text indexes for MVP).
- AI (optional): if you choose to add LLM features later, an external AI provider (Anthropic, OpenAI, or self-hosted) can be used for description generation, chat, or lead scoring. Keep any provider keys in Secrets Manager.
- SQS: asynchronous queue for indexing, thumbnails, long-running tasks.
- ECR + ECS: image registry and container orchestration for production.

Payments & Billing (INR) — overview
----------------------------------
This product charges per posting in INR and must integrate with Indian payment gateways (Razorpay, PayU, CCAvenue, etc.). Payments are handled by a dedicated Billing service inside the application boundary; the flow uses gateway-hosted checkout or tokenization to avoid storing card data and to reduce PCI scope.

High-level payment flow
- Client initiates a "purchase posting" request (POST /api/payments/initiate or part of POST /api/listings/{id}/publish)
- App/Billing creates a payment intent / order with the PG via server API and returns a checkout token or hosted-page URL to the client
- Client completes payment via PG (UPI QR, UPI intent, Cards, Netbanking, Wallet)
- PG sends a webhook to `/api/payments/webhook` (signed) with payment status
- Billing verifies webhook signature and idempotency, then marks the listing as PAID, creates an invoice (include GST details), and publishes events for indexing and settlement
- Daily/periodic reconciliation: background worker fetches settlement reports from PG, reconciles bank deposits, and creates payout batches to providers (if providers receive money)

Recommended DB tables (minimal)
- `payments` (id, listing_id, gateway_id, gateway_payment_id, amount_inr, currency, status, created_at, updated_at, metadata)
- `invoices` (id, payment_id, invoice_number, gst_amount, total_amount, client_details, issued_at)
- `ledger_entries` (id, payment_id, type:credit/debit, amount, balance_after, created_at)
- `payouts` (id, provider_id, amount, status, gateway_payout_id, executed_at)

Payouts, settlements & reconciliation
- Settlement window: PGs (and banks) remit funds after T+N days. Implement a reconciliation job that:
  - downloads PG settlement reports daily
  - matches settled transactions to `payments`
  - posts ledger entries and updates provider balances
  - creates payout batches and tracks payout status

KYC & Provider onboarding
- For receiving payouts (if providers receive funds on your platform), collect KYC documents and bank details. Use a KYC provider (Karza, Signzy) or integrate with Aadhaar/virtual KYC flows. Store KYC status and link to provider entity.

Compliance & security notes
- Use gateway-hosted checkout or tokenization (PG token) to avoid storing PAN/CVV—keeps your PCI scope minimal (SAQ-A possible).
- Verify PG webhook signatures and use idempotency keys to prevent double-processing.
- Keep all gateway secrets in AWS Secrets Manager (never in repo).
- Capture GST fields for invoices and keep copies of issued invoices (PDF or S3-presigned URLs).
- Implement refund and dispute workflows mapped to PG APIs.

Recommended endpoints (examples)
- POST /api/payments/initiate -> create payment intent / order
- GET /api/payments/{id}/status -> return payment / invoice status
- POST /api/payments/webhook -> receive gateway webhooks (signature validation)
- POST /api/payouts/{providerId}/request -> trigger provider payout (admin or scheduled)

Testing & sandbox
- Use PG sandbox/test credentials for end-to-end testing. Build automated tests that simulate webhook deliveries and use idempotency keys.

Operational & accounting
- Keep daily reconciliation jobs and logs for audits. Exportable CSV/ledger for finance teams.
- Keep a retained record of invoices for statutory compliance (GST rules in India).

Sequence examples (short)

1) Create listing with image upload
- Client POST /api/listings (metadata)
- Server inserts listing in Postgres as DRAFT
- Server returns S3 presigned PUT URL
- Client uploads image directly to S3
- Client requests publish -> Server verifies image, updates status, pushes index event to SQS
- Worker consumes event and indexes document into OpenSearch

2) Generate AI description
- Client requests auto-generated description
- Server reads Anthropic API key from Secrets Manager
- Server calls Anthropic completions API and receives text
- Server stores the description in Postgres and returns to client

Render the PlantUML file
------------------------
You can render `docs/architecture.puml` to PNG/SVG with PlantUML. Two easy options:

1) Using Docker (no install besides Docker):

```bash
# from repo root
docker run --rm -v "$(pwd):/workspace" plantuml/plantuml:latest -tpng /workspace/docs/architecture.puml
```

This will create `docs/architecture.png` in the repo.

2) VS Code: install the PlantUML extension (PlantUML or PlantUML Preview) and open the `.puml` file. The plugin can render diagrams and export PNG/SVG.

Next steps (recommended)
- Add a small `/api/ping` health endpoint and wire it to ALB health checks (already suggested earlier).
- Add a brief operational README in `docs/` describing deployment steps for ECR/ECS and Terraform skeleton.
- Create an OpenSearch indexing service or an async indexer that reads from SQS.

Files added
- `docs/architecture.puml` — PlantUML source (visual diagram + sequences)
- `docs/ARCHITECTURE.md` — this file

If you'd like, I can:
- Render the PlantUML to PNG and check it into `docs/architecture.png`.
- Add an `/api/ping` endpoint and a unit test.
- Scaffold `infra/` Terraform skeleton for ECR/RDS/ECS.

Pick any of the above and I will implement it next.

