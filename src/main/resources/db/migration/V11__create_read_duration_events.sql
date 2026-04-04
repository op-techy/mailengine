CREATE TABLE read_duration_events (
                                      id BIGSERIAL PRIMARY KEY,
                                      campaign_id BIGINT REFERENCES campaigns(id),
                                      recipient_id BIGINT REFERENCES recipients(id),
                                      checkpoint INTEGER NOT NULL,
                                      source VARCHAR(20),
                                      created_at TIMESTAMP DEFAULT NOW()
);