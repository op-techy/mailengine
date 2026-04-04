CREATE TABLE recipient_list_members (
                                        recipient_list_id BIGINT REFERENCES recipient_lists(id) ON DELETE CASCADE,
                                        recipient_id BIGINT REFERENCES recipients(id) ON DELETE CASCADE,
                                        PRIMARY KEY (recipient_list_id, recipient_id)
);