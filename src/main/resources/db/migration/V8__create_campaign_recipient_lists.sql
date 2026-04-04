CREATE TABLE campaign_recipient_lists (
                                          campaign_id BIGINT REFERENCES campaigns(id) ON DELETE CASCADE,
                                          recipient_list_id BIGINT REFERENCES recipient_lists(id),
                                          PRIMARY KEY (campaign_id, recipient_list_id)
);