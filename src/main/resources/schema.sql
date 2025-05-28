CREATE TABLE IF NOT EXISTS PRICES (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id INTEGER NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    price_list INTEGER NOT NULL,
    product_id BIGINT NOT NULL,
    priority INTEGER NOT NULL,
    price_amount DECIMAL(10,2) NOT NULL,
    curr VARCHAR(3) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_prices_lookup 
ON PRICES (brand_id, product_id, start_date, end_date, priority); 