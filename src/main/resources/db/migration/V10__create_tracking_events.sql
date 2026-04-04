CREATE TABLE tracking_events (
                                 id BIGSERIAL PRIMARY KEY,
                                 campaign_id BIGINT REFERENCES campaigns(id),
                                 recipient_id BIGINT REFERENCES recipients(id),
                                 event_type VARCHAR(30) NOT NULL,
                                 link_url TEXT,
                                 link_position VARCHAR(50),
                                 ip_address INET,
                                 geo_country VARCHAR(5),
                                 geo_city VARCHAR(100),
                                 device_type VARCHAR(20),
                                 email_client VARCHAR(50),
                                 os VARCHAR(50),
                                 user_agent TEXT,
                                 is_apple_proxy BOOLEAN DEFAULT FALSE,
                                 created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_tracking_campaign ON tracking_events(campaign_id);
CREATE INDEX idx_tracking_recipient ON tracking_events(recipient_id);
CREATE INDEX idx_tracking_type ON tracking_events(event_type);