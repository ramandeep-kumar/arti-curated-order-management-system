-- Create returns table
CREATE TABLE returns (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    reason VARCHAR(500) NOT NULL,
    current_state VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
    approved_by VARCHAR(100),
    tracking_number VARCHAR(100),
    refund_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create return state history table
CREATE TABLE return_state_history (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL REFERENCES returns(id) ON DELETE CASCADE,
    from_state VARCHAR(50),
    to_state VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    changed_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for returns
CREATE INDEX idx_returns_order_id ON returns(order_id);
CREATE INDEX idx_returns_state ON returns(current_state);
CREATE INDEX idx_returns_number ON returns(return_number);
CREATE INDEX idx_returns_created_at ON returns(created_at);

CREATE INDEX idx_return_history_return_id ON return_state_history(return_id);
CREATE INDEX idx_return_history_changed_at ON return_state_history(changed_at);

-- Note: Business logic validation for returns should be handled in application code
-- PostgreSQL doesn't support subqueries in check constraints
-- The constraint below has been removed and will be enforced at the application level
