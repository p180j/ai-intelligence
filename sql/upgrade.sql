-- Phase 6 upgrade. Execute once on a Phase 4/5 database.
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

-- Reader Experience V2. Execute once after Phase 6.
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
UPDATE ai_event SET title=REPLACE(title,'（已由管理员复核）',''),summary=REPLACE(summary,'（已由管理员复核）',''),what_happened=REPLACE(what_happened,'（已由管理员复核）',''),why_important=REPLACE(why_important,'（已由管理员复核）',''),developer_impact=REPLACE(developer_impact,'（已由管理员复核）','');

-- Normalize legacy Event workflow combinations. New state changes must use the workflow actions.
UPDATE ai_event SET publish_status='DRAFT',published_at=NULL,updated_at=CURRENT_TIMESTAMP WHERE review_status<>'APPROVED' AND publish_status<>'DRAFT';
UPDATE ai_event SET published_at=NULL,updated_at=CURRENT_TIMESTAMP WHERE publish_status='DRAFT' AND published_at IS NOT NULL;
UPDATE ai_event SET published_at=COALESCE(published_at,updated_at,created_at) WHERE review_status='APPROVED' AND publish_status='PUBLISHED' AND published_at IS NULL;
