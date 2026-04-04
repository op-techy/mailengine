CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       account_id BIGINT REFERENCES accounts(id),
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('admin','editor','viewer')),
                       email_verified BOOLEAN DEFAULT FALSE,
                       must_change_pwd BOOLEAN DEFAULT FALSE,
                       invited_by BIGINT REFERENCES users(id),
                       last_login TIMESTAMP,
                       created_at TIMESTAMP DEFAULT NOW()
);