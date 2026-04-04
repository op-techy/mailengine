CREATE TABLE recipient_lists (
                                 id BIGSERIAL PRIMARY KEY,
                                 account_id BIGINT REFERENCES accounts(id),
                                 name VARCHAR(255) NOT NULL,
                                 description TEXT,
                                 recipient_count INTEGER DEFAULT 0,
                                 created_at TIMESTAMP DEFAULT NOW()
);