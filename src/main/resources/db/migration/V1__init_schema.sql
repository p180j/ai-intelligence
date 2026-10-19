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
