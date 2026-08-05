CREATE TABLE order_items(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_name VARCHAR(160) NOT NULL,
    product_maker VARCHAR(120) NOT NULL,
    product_serial VARCHAR(80) NOT NULL,
    unit_price_cents BIGINT NOT NULL CHECK (unit_price_cents >= 0),
    UNIQUE (order_id,product_id)
);

CREATE INDEX idx_order_items_id ON order_items(order_id);