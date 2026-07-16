CREATE TABLE IF NOT EXISTS order_view (
    order_id UUID PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);