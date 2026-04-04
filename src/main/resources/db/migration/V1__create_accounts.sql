CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          company_name VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP DEFAULT NOW()
);