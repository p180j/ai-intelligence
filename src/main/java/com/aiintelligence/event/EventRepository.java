package com.aiintelligence.event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {
    private final JdbcTemplate jdbcTemplate;
    public EventRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<Event> findAll() { return jdbcTemplate.query("SELECT * FROM ai_event ORDER BY COALESCE(event_time,created_at) DESC,id DESC", rowMapper()); }
    public List<Event> findRecent(LocalDateTime since) { return jdbcTemplate.query("SELECT * FROM ai_event WHERE COALESCE(event_time,created_at)>=? ORDER BY COALESCE(event_time,created_at) DESC LIMIT 20", rowMapper(), Timestamp.valueOf(since)); }
    public void updateReviewStatus(long id, ReviewStatus status) { jdbcTemplate.update("UPDATE ai_event SET review_status=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",status.name(),id); }
    public void publish(long id) { jdbcTemplate.update("UPDATE ai_event SET publish_status='PUBLISHED',published_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE id=?",id); }
    public void offline(long id) { jdbcTemplate.update("UPDATE ai_event SET publish_status='OFFLINE',updated_at=CURRENT_TIMESTAMP WHERE id=?",id); }
    public List<Event> searchPublished(String keyword) { String value="%"+(keyword==null?"":keyword.toLowerCase())+"%";return jdbcTemplate.query("SELECT * FROM ai_event WHERE review_status='APPROVED' AND publish_status='PUBLISHED' AND (LOWER(title) LIKE ? OR LOWER(summary) LIKE ?) ORDER BY COALESCE(event_time,created_at) DESC LIMIT 50",rowMapper(),value,value); }
    public Optional<Event> findById(long id) { return jdbcTemplate.query("SELECT * FROM ai_event WHERE id=?", rowMapper(), id).stream().findFirst(); }
    public Optional<Event> findByArticleId(long articleId) { return jdbcTemplate.query("SELECT e.* FROM ai_event e JOIN ai_event_article ea ON ea.event_id=e.id WHERE ea.article_id=?",rowMapper(),articleId).stream().findFirst(); }

    public long insert(EventRequest r) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(c -> {
            java.sql.PreparedStatement s = c.prepareStatement("INSERT INTO ai_event (title,summary,what_happened,why_important,availability,developer_impact,category,status,impact_score,technical_score,availability_score,developer_value_score,credibility_score,freshness_score,total_score,event_time,review_status,publish_status,published_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'PENDING','DRAFT',NULL)", new String[]{"id"});
            bindContent(s, r); return s;
        }, key);
        return key.getKey().longValue();
    }

    public int update(long id, EventRequest r) {
        return jdbcTemplate.update(c -> {
            java.sql.PreparedStatement s = c.prepareStatement("UPDATE ai_event SET title=?,summary=?,what_happened=?,why_important=?,availability=?,developer_impact=?,category=?,status=?,impact_score=?,technical_score=?,availability_score=?,developer_value_score=?,credibility_score=?,freshness_score=?,total_score=?,event_time=?,updated_at=CURRENT_TIMESTAMP WHERE id=?");
            bindContent(s, r); s.setLong(17, id); return s;
        });
    }

    public void attachArticle(long eventId, long articleId, ArticleRelationRequest r) {
        int updated = jdbcTemplate.update("UPDATE ai_event_article SET relation_type=?,is_primary_source=? WHERE event_id=? AND article_id=?",
                r.relationType.name(), Boolean.TRUE.equals(r.primarySource), eventId, articleId);
        if (updated == 0) jdbcTemplate.update("INSERT INTO ai_event_article (event_id,article_id,relation_type,is_primary_source) VALUES (?,?,?,?)",
                eventId, articleId, r.relationType.name(), Boolean.TRUE.equals(r.primarySource));
    }

    public int detachArticle(long eventId, long articleId) { return jdbcTemplate.update("DELETE FROM ai_event_article WHERE event_id=? AND article_id=?", eventId, articleId); }

    public List<EventArticle> findArticles(long eventId) {
        return jdbcTemplate.query("SELECT a.id article_id,a.source_id,s.name source_name,a.title,a.url,a.published_at,ea.relation_type,ea.is_primary_source FROM ai_event_article ea JOIN ai_article a ON a.id=ea.article_id JOIN ai_source s ON s.id=a.source_id WHERE ea.event_id=? ORDER BY ea.is_primary_source DESC,a.published_at DESC,a.id", (rs,n) -> {
            EventArticle a = new EventArticle(); a.articleId=rs.getLong("article_id"); a.sourceId=rs.getLong("source_id"); a.sourceName=rs.getString("source_name"); a.title=rs.getString("title"); a.url=rs.getString("url"); a.publishedAt=local(rs.getTimestamp("published_at")); a.relationType=ArticleRelationType.valueOf(rs.getString("relation_type")); a.primarySource=rs.getBoolean("is_primary_source"); return a;
        }, eventId);
    }

    public List<EventProduct> findProducts(long eventId) {
        return jdbcTemplate.query("SELECT p.id product_id,p.name,p.slug,pe.relation_type FROM ai_product_event pe JOIN ai_product p ON p.id=pe.product_id WHERE pe.event_id=? ORDER BY p.name", (rs,n) -> {
            EventProduct p = new EventProduct(); p.productId=rs.getLong("product_id"); p.name=rs.getString("name"); p.slug=rs.getString("slug"); p.relationType=rs.getString("relation_type"); return p;
        }, eventId);
    }

    private void bindContent(java.sql.PreparedStatement s, EventRequest r) throws SQLException {
        s.setString(1,r.title); s.setString(2,r.summary); s.setString(3,r.whatHappened); s.setString(4,r.whyImportant); s.setString(5,r.availability); s.setString(6,r.developerImpact);
        s.setString(7,(r.category==null?EventCategory.NORMAL:r.category).name()); s.setString(8,r.status); setInteger(s,9,r.impactScore); setInteger(s,10,r.technicalScore); setInteger(s,11,r.availabilityScore); setInteger(s,12,r.developerValueScore); setInteger(s,13,r.credibilityScore); setInteger(s,14,r.freshnessScore); setInteger(s,15,r.totalScore);
        s.setTimestamp(16,timestamp(r.eventTime));
    }
    private void setInteger(java.sql.PreparedStatement s,int i,Integer v) throws SQLException { if(v==null)s.setNull(i,java.sql.Types.INTEGER);else s.setInt(i,v); }
    private RowMapper<Event> rowMapper() { return (rs,n)->map(rs); }
    public RowMapper<Event> rowMapperForReuse() { return rowMapper(); }
    private Event map(ResultSet rs) throws SQLException {
        Event e=new Event(); e.id=rs.getLong("id"); e.title=rs.getString("title"); e.summary=rs.getString("summary"); e.whatHappened=rs.getString("what_happened"); e.whyImportant=rs.getString("why_important"); e.availability=rs.getString("availability"); e.developerImpact=rs.getString("developer_impact"); e.category=EventCategory.valueOf(rs.getString("category")); e.status=rs.getString("status"); e.impactScore=integer(rs,"impact_score"); e.technicalScore=integer(rs,"technical_score"); e.availabilityScore=integer(rs,"availability_score"); e.developerValueScore=integer(rs,"developer_value_score"); e.credibilityScore=integer(rs,"credibility_score"); e.freshnessScore=integer(rs,"freshness_score"); e.totalScore=integer(rs,"total_score"); e.eventTime=local(rs.getTimestamp("event_time")); e.reviewStatus=ReviewStatus.valueOf(rs.getString("review_status")); e.publishStatus=PublishStatus.valueOf(rs.getString("publish_status")); e.createdAt=local(rs.getTimestamp("created_at")); e.updatedAt=local(rs.getTimestamp("updated_at")); e.publishedAt=local(rs.getTimestamp("published_at")); return e;
    }
    private Integer integer(ResultSet rs,String name)throws SQLException{int v=rs.getInt(name);return rs.wasNull()?null:v;}
    private Timestamp timestamp(LocalDateTime v){return v==null?null:Timestamp.valueOf(v);} private static LocalDateTime local(Timestamp v){return v==null?null:v.toLocalDateTime();}
}
