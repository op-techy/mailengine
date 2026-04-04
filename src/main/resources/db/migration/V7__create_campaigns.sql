CREATE TABLE campaigns (
                           id BIGSERIAL PRIMARY KEY,
                           account_id BIGINT REFERENCES accounts(id),
                           name VARCHAR(255) NOT NULL,
                           subject_line VARCHAR(500) NOT NULL,
                           from_name VARCHAR(255) NOT NULL,
                           from_email VARCHAR(255) NOT NULL,
                           template_id BIGINT REFERENCES templates(id),
                           status VARCHAR(20) DEFAULT 'draft'
                               CHECK (status IN
                                      ('draft','scheduled','sending','sent','cancelled','failed')),
                           scheduled_at TIMESTAMP,
                           timezone VARCHAR(50),
                           sent_at TIMESTAMP,
                           created_by BIGINT REFERENCES users(id),
                           total_recipients INTEGER DEFAULT 0,
                           total_sent INTEGER DEFAULT 0,
                           total_delivered INTEGER DEFAULT 0,
                           total_bounced INTEGER DEFAULT 0,
                           total_opened INTEGER DEFAULT 0,
                           total_clicked INTEGER DEFAULT 0,
                           total_complained INTEGER DEFAULT 0,
                           total_unsubscribed INTEGER DEFAULT 0,
                           created_at TIMESTAMP DEFAULT NOW()
);