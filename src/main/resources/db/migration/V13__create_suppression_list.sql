CREATE TABLE suppression_list (
                                  id BIGSERIAL PRIMARY KEY,
                                  account_id BIGINT REFERENCES accounts(id),
                                  email VARCHAR(255) NOT NULL,
                                  reason VARCHAR(20),
                                  created_at TIMESTAMP DEFAULT NOW(),
                                  UNIQUE(account_id, email)
);