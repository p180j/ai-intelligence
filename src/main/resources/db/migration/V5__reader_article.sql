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

UPDATE ai_event SET
    title=REPLACE(title,'（已由管理员复核）',''),
    summary=REPLACE(summary,'（已由管理员复核）',''),
    what_happened=REPLACE(what_happened,'（已由管理员复核）',''),
    why_important=REPLACE(why_important,'（已由管理员复核）',''),
    developer_impact=REPLACE(developer_impact,'（已由管理员复核）','');
