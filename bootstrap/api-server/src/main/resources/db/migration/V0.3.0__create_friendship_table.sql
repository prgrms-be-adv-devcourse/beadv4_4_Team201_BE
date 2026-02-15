CREATE TABLE IF NOT EXISTS friendships (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id  BIGINT NOT NULL,
    receiver_id   BIGINT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at   TIMESTAMP NULL,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),

    CONSTRAINT uk_friendship_pair UNIQUE (requester_id, receiver_id),
    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES members(id),
    CONSTRAINT fk_friendship_receiver FOREIGN KEY (receiver_id) REFERENCES members(id)
);

CREATE INDEX idx_friendship_receiver_status ON friendships (receiver_id, status);
CREATE INDEX idx_friendship_requester_status ON friendships (requester_id, status);
