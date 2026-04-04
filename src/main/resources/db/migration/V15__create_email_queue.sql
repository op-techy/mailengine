CREATE TABLE email_queue (
                             id BIGSERIAL PRIMARY KEY,
                             campaign_id BIGINT REFERENCES campaigns(id),
                             recipient_id BIGINT REFERENCES recipients(id),
                             to_email VARCHAR(255) NOT NULL,
                             html_content TEXT NOT NULL,
                             subject_line VARCHAR(500) NOT NULL,
                             from_name VARCHAR(255) NOT NULL,
                             from_email VARCHAR(255) NOT NULL,
                             status VARCHAR(20) DEFAULT 'pending'
                                 CHECK (status IN ('pending','sending','sent','failed')),
                             attempts INTEGER DEFAULT 0,
                             error_message TEXT,
                             locked_until TIMESTAMP,
                             created_at TIMESTAMP DEFAULT NOW(),
                             sent_at TIMESTAMP
);
CREATE INDEX idx_email_queue_status ON email_queue(status, locked_until);