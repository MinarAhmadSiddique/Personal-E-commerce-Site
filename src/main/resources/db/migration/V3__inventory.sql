CREATE TABLE inventory(
    product_id BIGINT PRIMARY KEY REFERENCES products(id),
    location VARCHAR(10) NOT NULL DEFAULT 'FLOOR' CHECK (location IN ('FLOOR','BENCH','HOLD','SOLD')),
    hold_for VARCHAR(255),
    hold_until TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventory_location ON inventory(location);