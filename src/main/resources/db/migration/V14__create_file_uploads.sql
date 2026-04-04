CREATE TABLE file_uploads (
                              id BIGSERIAL PRIMARY KEY,
                              account_id BIGINT REFERENCES accounts(id),
                              recipient_list_id BIGINT REFERENCES recipient_lists(id),
                              file_name VARCHAR(255),
                              status VARCHAR(20) DEFAULT 'processing',
                              total_rows INTEGER,
                              imported_rows INTEGER,
                              skipped_rows INTEGER,
                              duplicate_rows INTEGER,
                              column_preview JSONB,
                              column_mapping JSONB,
                              created_at TIMESTAMP DEFAULT NOW()
);