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
