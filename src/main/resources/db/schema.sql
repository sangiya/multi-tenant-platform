CREATE TABLE IF NOT EXISTS products (
    id          VARCHAR(36)     NOT NULL PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(1000),
    price       DECIMAL(19, 4)  NOT NULL,
    stock_quantity INT           NOT NULL,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP
);
