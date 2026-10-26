-- Complete MySQL schema baseline. Execute against an empty database.
CREATE TABLE ai_source (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    base_url VARCHAR(1000),
    feed_url VARCHAR(1000),
    priority INT NOT NULL DEFAULT 0,
    language VARCHAR(32),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_collect_time TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE ai_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    url VARCHAR(750) NOT NULL,
    author VARCHAR(200),
    language VARCHAR(32),
    published_at TIMESTAMP NULL,
    raw_content TEXT,
    clean_content TEXT,
    content_hash VARCHAR(128),
    collect_status VARCHAR(32) NOT NULL DEFAULT 'NEW',
    collected_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_article_url UNIQUE (url),
    CONSTRAINT fk_ai_article_source FOREIGN KEY (source_id) REFERENCES ai_source (id)
);
CREATE INDEX idx_ai_article_content_hash ON ai_article (content_hash);
CREATE INDEX idx_ai_article_source_status ON ai_article (source_id, collect_status);
CREATE INDEX idx_ai_article_published_at ON ai_article (published_at);

CREATE TABLE ai_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    summary TEXT,
    what_happened TEXT,
    why_important TEXT,
    availability TEXT,
    developer_impact TEXT,
    category VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(64),
    impact_score INT,
    technical_score INT,
    availability_score INT,
    developer_value_score INT,
    credibility_score INT,
    freshness_score INT,
    total_score INT,
    event_time TIMESTAMP NULL,
    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_ai_event_time ON ai_event (event_time);
CREATE INDEX idx_ai_event_review_publish ON ai_event (review_status, publish_status);

CREATE TABLE ai_event_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    relation_type VARCHAR(32) NOT NULL,
    is_primary_source BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_event_article UNIQUE (event_id, article_id),
    CONSTRAINT fk_ai_event_article_event FOREIGN KEY (event_id) REFERENCES ai_event (id),
    CONSTRAINT fk_ai_event_article_article FOREIGN KEY (article_id) REFERENCES ai_article (id)
);
CREATE INDEX idx_ai_event_article_article ON ai_event_article (article_id);

CREATE TABLE ai_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    summary TEXT,
    product_type VARCHAR(64),
    company_name VARCHAR(200),
    official_url VARCHAR(1000),
    github_url VARCHAR(1000),
    docs_url VARCHAR(1000),
    api_url VARCHAR(1000),
    open_source BOOLEAN NOT NULL DEFAULT FALSE,
    availability VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_product_slug UNIQUE (slug)
);

CREATE TABLE ai_product_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    relation_type VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_product_event UNIQUE (product_id, event_id),
    CONSTRAINT fk_ai_product_event_product FOREIGN KEY (product_id) REFERENCES ai_product (id),
    CONSTRAINT fk_ai_product_event_event FOREIGN KEY (event_id) REFERENCES ai_event (id)
);
CREATE INDEX idx_ai_product_event_event ON ai_product_event (event_id);

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

CREATE TABLE ai_daily_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    report_date DATE NOT NULL,
    title VARCHAR(500) NOT NULL,
    summary TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_daily_report_date UNIQUE (report_date)
);

CREATE TABLE ai_daily_report_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    section VARCHAR(32) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_daily_report_event UNIQUE (report_id, event_id),
    CONSTRAINT fk_ai_report_event_report FOREIGN KEY (report_id) REFERENCES ai_daily_report (id),
    CONSTRAINT fk_ai_report_event_event FOREIGN KEY (event_id) REFERENCES ai_event (id)
);
CREATE INDEX idx_ai_report_event_order ON ai_daily_report_event (report_id, display_order);

ALTER TABLE ai_product ADD COLUMN capability_tags VARCHAR(1000) NULL;

CREATE TABLE ai_page_view (
    id BIGINT NOT NULL AUTO_INCREMENT,
    page_type VARCHAR(32) NOT NULL,
    event_id BIGINT NULL,
    product_id BIGINT NULL,
    referrer VARCHAR(1000),
    user_agent VARCHAR(1000),
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_ai_page_view_event ON ai_page_view (event_id, viewed_at);
CREATE INDEX idx_ai_page_view_product ON ai_page_view (product_id, viewed_at);

CREATE TABLE ai_event_quality_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    quality_result VARCHAR(32) NOT NULL,
    wrong_merge BOOLEAN NOT NULL DEFAULT FALSE,
    duplicate_event_id BIGINT NULL,
    notes VARCHAR(2000),
    review_seconds INT,
    reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_event_quality_event UNIQUE (event_id),
    CONSTRAINT fk_ai_event_quality_event FOREIGN KEY (event_id) REFERENCES ai_event (id)
);
CREATE INDEX idx_ai_event_quality_time ON ai_event_quality_feedback (reviewed_at);
CREATE TABLE ai_event_eval_case (
    id BIGINT NOT NULL AUTO_INCREMENT,
    article_a_id BIGINT NOT NULL,
    article_b_id BIGINT NOT NULL,
    expected_result VARCHAR(32) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    feedback_event_id BIGINT NULL,
    notes VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_eval_article_pair UNIQUE (article_a_id, article_b_id),
    CONSTRAINT fk_ai_eval_article_a FOREIGN KEY (article_a_id) REFERENCES ai_article (id),
    CONSTRAINT fk_ai_eval_article_b FOREIGN KEY (article_b_id) REFERENCES ai_article (id)
);
CREATE INDEX idx_ai_eval_expected ON ai_event_eval_case (expected_result, created_at);
ALTER TABLE ai_daily_report_event ADD COLUMN quality_feedback VARCHAR(32) NULL;
ALTER TABLE ai_page_view ADD COLUMN report_id BIGINT NULL;
CREATE INDEX idx_ai_page_view_report ON ai_page_view (report_id, viewed_at);

CREATE TABLE ai_event_reader_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    reader_title VARCHAR(500) NOT NULL,
    reader_lead TEXT,
    reader_content TEXT NOT NULL,
    reader_conclusion TEXT,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    prompt_version VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_reader_event UNIQUE (event_id),
    CONSTRAINT fk_ai_reader_event FOREIGN KEY (event_id) REFERENCES ai_event (id)
);

-- Default official AI sources verified on 2026-10-22.
INSERT INTO ai_source (name, source_type, base_url, feed_url, priority, language, enabled, collector_type, collect_config, authority_level, is_official)
VALUES ('Google AI Blog', 'RSS', 'https://blog.google/innovation-and-ai/technology/ai/', 'https://blog.google/innovation-and-ai/technology/ai/rss/', 95, 'en', TRUE, 'RSS', '{"maxItems": 5}', 10, TRUE);
INSERT INTO ai_source (name, source_type, base_url, feed_url, priority, language, enabled, collector_type, collect_config, authority_level, is_official)
VALUES ('AWS Machine Learning Blog', 'RSS', 'https://aws.amazon.com/blogs/machine-learning/', 'https://aws.amazon.com/blogs/machine-learning/feed/', 85, 'en', TRUE, 'RSS', '{"maxItems": 5}', 9, TRUE);
INSERT INTO ai_source (name, source_type, base_url, feed_url, priority, language, enabled, collector_type, collect_config, authority_level, is_official)
VALUES ('NVIDIA AI Blog', 'RSS', 'https://blogs.nvidia.com/', 'https://blogs.nvidia.com/feed/', 85, 'en', TRUE, 'RSS', '{"maxItems": 5}', 9, TRUE);
