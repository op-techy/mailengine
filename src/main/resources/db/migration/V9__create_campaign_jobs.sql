CREATE TABLE campaign_jobs (
                               id BIGSERIAL PRIMARY KEY,
                               campaign_id BIGINT REFERENCES campaigns(id) UNIQUE,
                               status VARCHAR(20) DEFAULT 'queued'
                                   CHECK (status IN
                                          ('queued','processing','paused','completed','failed','cancelled')),
                               total_emails INTEGER DEFAULT 0,
                               queued_count INTEGER DEFAULT 0,
                               sent_count INTEGER DEFAULT 0,
                               failed_count INTEGER DEFAULT 0,
                               started_at TIMESTAMP,
                               completed_at TIMESTAMP,
                               error_message TEXT,
                               created_at TIMESTAMP DEFAULT NOW(),
                               updated_at TIMESTAMP DEFAULT NOW()
);