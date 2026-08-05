CREATE TABLE products
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    slug          VARCHAR(120) NOT NULL UNIQUE,
    serial_number VARCHAR(80)  NOT NULL UNIQUE,
    name          VARCHAR(160) NOT NULL,
    maker         INTEGER,
    price_cents   BIGINT       NOT NULL CHECK (price_cents >= 0),
    grade         CHAR(1) CHECK (grade IN ('A', 'B', 'C', 'D')),
    category      VARCHAR(40)  NOT NULL,
    category_slug VARCHAR(60)  NOT NULL,
    blurb         TEXT,
    panel_json    JSONB,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_category_slug ON products (category_slug);
CREATE INDEX idx_products_active ON products(active);