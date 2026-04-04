CREATE TABLE templates (
                           id BIGSERIAL PRIMARY KEY,
                           account_id BIGINT REFERENCES accounts(id),
                           name VARCHAR(255) NOT NULL,
                           category VARCHAR(100),
                           html_content TEXT NOT NULL,
                           json_design JSONB,
                           thumbnail_url VARCHAR(500),
                           created_by BIGINT REFERENCES users(id),
                           created_at TIMESTAMP DEFAULT NOW(),
                           updated_at TIMESTAMP DEFAULT NOW()
);