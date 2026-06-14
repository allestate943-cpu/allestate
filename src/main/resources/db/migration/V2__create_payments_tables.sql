-- Flyway migration: create payments and related tables

CREATE TABLE IF NOT EXISTS payments (
  id BIGSERIAL PRIMARY KEY,
  listing_id BIGINT,
  user_id BIGINT,
  amount_in_paise BIGINT NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'INR',
  gateway_order_id VARCHAR(255),
  gateway_payment_id VARCHAR(255),
  gateway_event_id VARCHAR(255),
  status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
  payment_method VARCHAR(100),
  metadata JSONB,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_gateway_payment_id ON payments(gateway_payment_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_gateway_event_id ON payments(gateway_event_id);

CREATE TABLE IF NOT EXISTS invoices (
  id BIGSERIAL PRIMARY KEY,
  payment_id BIGINT REFERENCES payments(id) ON DELETE CASCADE,
  invoice_number VARCHAR(100),
  gst_amount NUMERIC(18,2),
  taxable_amount NUMERIC(18,2),
  total_amount NUMERIC(18,2),
  invoice_pdf_s3_key VARCHAR(1024),
  issued_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ledger_entries (
  id BIGSERIAL PRIMARY KEY,
  payment_id BIGINT REFERENCES payments(id) ON DELETE SET NULL,
  type VARCHAR(20),
  amount_in_paise BIGINT,
  balance_after_in_paise BIGINT,
  entry_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  notes TEXT
);

CREATE TABLE IF NOT EXISTS payouts (
  id BIGSERIAL PRIMARY KEY,
  provider_id BIGINT,
  amount_in_paise BIGINT,
  status VARCHAR(50) DEFAULT 'PENDING',
  gateway_payout_id VARCHAR(255),
  executed_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS gateway_events (
  id BIGSERIAL PRIMARY KEY,
  gateway_event_id VARCHAR(255) UNIQUE,
  payload JSONB,
  processed BOOLEAN DEFAULT false,
  received_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

