-- Migration: create invoices table
CREATE TABLE IF NOT EXISTS invoices (
  id BIGSERIAL PRIMARY KEY,
  invoice_number VARCHAR(255) NOT NULL UNIQUE,
  order_id BIGINT NOT NULL,
  amount NUMERIC(10,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at TIMESTAMP,
  issued_at TIMESTAMP,
  paid_at TIMESTAMP
);
ALTER TABLE invoices ADD CONSTRAINT IF NOT EXISTS fk_invoices_order FOREIGN KEY (order_id) REFERENCES orders(id);
