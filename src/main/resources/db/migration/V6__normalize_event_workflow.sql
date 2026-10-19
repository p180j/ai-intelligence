UPDATE ai_event
SET publish_status = 'DRAFT', published_at = NULL, updated_at = CURRENT_TIMESTAMP
WHERE review_status <> 'APPROVED' AND publish_status <> 'DRAFT';

UPDATE ai_event
SET published_at = NULL, updated_at = CURRENT_TIMESTAMP
WHERE publish_status = 'DRAFT' AND published_at IS NOT NULL;

UPDATE ai_event
SET published_at = COALESCE(published_at, updated_at, created_at)
WHERE review_status = 'APPROVED' AND publish_status = 'PUBLISHED' AND published_at IS NULL;
