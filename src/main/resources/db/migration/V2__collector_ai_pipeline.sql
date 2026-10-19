ALTER TABLE ai_source ADD COLUMN collector_type VARCHAR(32) NULL;
ALTER TABLE ai_source ADD COLUMN collect_config TEXT NULL;
ALTER TABLE ai_source ADD COLUMN last_success_time TIMESTAMP NULL;
ALTER TABLE ai_source ADD COLUMN last_error_message VARCHAR(1000) NULL;
ALTER TABLE ai_source ADD COLUMN failure_count INT NOT NULL DEFAULT 0;
ALTER TABLE ai_source ADD COLUMN last_collect_count INT NOT NULL DEFAULT 0;
ALTER TABLE ai_source ADD COLUMN authority_level INT NOT NULL DEFAULT 5;
ALTER TABLE ai_source ADD COLUMN is_official BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE ai_event ADD COLUMN availability_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE ai_article ADD CONSTRAINT uk_ai_article_content_hash UNIQUE (content_hash);

CREATE TABLE ai_processing_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    article_id BIGINT NULL,
    event_id BIGINT NULL,
    request_type VARCHAR(50) NOT NULL,
    ai_provider VARCHAR(50) NOT NULL,
    ai_model VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    input_tokens INT,
    output_tokens INT,
    estimated_cost DECIMAL(12,6),
    latency_ms BIGINT,
    error_message VARCHAR(1000),
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_ai_processing_article ON ai_processing_log (article_id, processed_at);
CREATE INDEX idx_ai_processing_event ON ai_processing_log (event_id, processed_at);

CREATE TABLE ai_product_candidate (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_product_candidate UNIQUE (event_id, name),
    CONSTRAINT fk_ai_product_candidate_event FOREIGN KEY (event_id) REFERENCES ai_event (id)
);

