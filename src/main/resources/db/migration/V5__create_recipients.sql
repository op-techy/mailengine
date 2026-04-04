CREATE TABLE recipients (
                            id BIGSERIAL PRIMARY KEY,
                            account_id BIGINT REFERENCES accounts(id),
                            email VARCHAR(255) NOT NULL,
                            first_name VARCHAR(255),
                            last_name VARCHAR(255),
                            company VARCHAR(255),
                            custom_fields JSONB DEFAULT '{}',
                            status VARCHAR(20) DEFAULT 'active'
                                CHECK (status IN ('active','unsubscribed','bounced','complained')),
                            created_at TIMESTAMP DEFAULT NOW(),
                            UNIQUE(account_id, email)
);