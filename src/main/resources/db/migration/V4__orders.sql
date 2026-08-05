CREATE TABLE orders(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(10) NOT NULL DEFAULT 'CREATED' CHECK ( status IN ('CREATED','PAID','SHIPPED','DELIVERED','CANCELLED')),
    subtotal_cents BIGINT NOT NULL CHECK ( subtotal_cents >= 0 ),
    shipping_cents BIGINT NOT NULL DEFAULT 2500 CHECK ( shipping_cents >=0 ),
    total_cents BIGINT NOT NULL CHECK ( total_cents >= 0 ),
    ship_name VARCHAR(160) NOT NULL,
    ship_line1 VARCHAR(200) NOT NULL,
    ship_city VARCHAR(100) NOT NULL,
    ship_state CHAR(2) NOT NULL,
    ship_zip VARCHAR(10) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    placed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);