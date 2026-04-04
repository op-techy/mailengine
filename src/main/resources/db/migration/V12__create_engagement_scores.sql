CREATE TABLE engagement_scores (
                                   recipient_id BIGINT PRIMARY KEY REFERENCES recipients(id),
                                   account_id BIGINT REFERENCES accounts(id),
                                   score DECIMAL(5,2) DEFAULT 0,
                                   category VARCHAR(20),
                                   last_open_at TIMESTAMP,
                                   last_click_at TIMESTAMP,
                                   campaigns_sent INTEGER DEFAULT 0,
                                   campaigns_opened INTEGER DEFAULT 0,
                                   campaigns_clicked INTEGER DEFAULT 0,
                                   updated_at TIMESTAMP DEFAULT NOW()
);