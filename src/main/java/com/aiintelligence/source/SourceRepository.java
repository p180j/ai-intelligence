package com.aiintelligence.source;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class SourceRepository {
    private final JdbcTemplate jdbcTemplate;

    public SourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Source> findAll() {
        return jdbcTemplate.query("SELECT * FROM ai_source ORDER BY priority DESC, id DESC", rowMapper());
    }

    public Optional<Source> findById(long id) {
        return jdbcTemplate.query("SELECT * FROM ai_source WHERE id = ?", rowMapper(), id).stream().findFirst();
    }

    public long insert(SourceRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ai_source (name, source_type, base_url, feed_url, priority, language, enabled, last_collect_time, collector_type, collect_config, authority_level, is_official) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"});
            statement.setString(1, request.name);
            statement.setString(2, request.sourceType.name());
            statement.setString(3, request.baseUrl);
            statement.setString(4, request.feedUrl);
            statement.setInt(5, request.priority == null ? 0 : request.priority);
            statement.setString(6, request.language);
            statement.setBoolean(7, request.enabled == null || request.enabled);
            statement.setTimestamp(8, request.lastCollectTime == null ? null : Timestamp.valueOf(request.lastCollectTime));
            statement.setString(9, request.collectorType);
            statement.setString(10, request.collectConfig);
            statement.setInt(11, request.authorityLevel == null ? 5 : request.authorityLevel);
            statement.setBoolean(12, Boolean.TRUE.equals(request.official));
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int update(long id, SourceRequest request) {
        return jdbcTemplate.update("UPDATE ai_source SET name=?, source_type=?, base_url=?, feed_url=?, priority=?, language=?, enabled=?, collector_type=?, collect_config=?, authority_level=?, is_official=?, updated_at=CURRENT_TIMESTAMP WHERE id=?",
                request.name, request.sourceType.name(), request.baseUrl, request.feedUrl, request.priority == null ? 0 : request.priority,
                request.language, request.enabled == null || request.enabled, request.collectorType, request.collectConfig,
                request.authorityLevel == null ? 5 : request.authorityLevel, Boolean.TRUE.equals(request.official), id);
    }

    public int disable(long id) {
        return jdbcTemplate.update("UPDATE ai_source SET enabled=FALSE, updated_at=CURRENT_TIMESTAMP WHERE id=?", id);
    }

    public int enable(long id) {
        return jdbcTemplate.update("UPDATE ai_source SET enabled=TRUE,failure_count=0,last_error_message=NULL,updated_at=CURRENT_TIMESTAMP WHERE id=?", id);
    }

    public int delete(long id) {
        return jdbcTemplate.update("DELETE FROM ai_source WHERE id=?", id);
    }

    public int countArticles(long id) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_article WHERE source_id=?", Integer.class, id);
    }

    public List<Source> findEnabled() {
        return jdbcTemplate.query("SELECT * FROM ai_source WHERE enabled=TRUE ORDER BY priority DESC,id", rowMapper());
    }

    public void collectSucceeded(long id, int count) {
        jdbcTemplate.update("UPDATE ai_source SET last_collect_time=CURRENT_TIMESTAMP,last_success_time=CURRENT_TIMESTAMP,last_error_message=NULL,failure_count=0,last_collect_count=?,updated_at=CURRENT_TIMESTAMP WHERE id=?", count, id);
    }

    public void collectFailed(long id, String message) {
        jdbcTemplate.update("UPDATE ai_source SET last_collect_time=CURRENT_TIMESTAMP,last_error_message=?,failure_count=failure_count+1,last_collect_count=0,updated_at=CURRENT_TIMESTAMP WHERE id=?", message, id);
    }

    public void disableWhenFailureCountExceeds(long id, int threshold) {
        jdbcTemplate.update("UPDATE ai_source SET enabled=FALSE,updated_at=CURRENT_TIMESTAMP WHERE id=? AND failure_count>?", id, threshold);
    }

    public void disableExceededFailures(int threshold) {
        jdbcTemplate.update("UPDATE ai_source SET enabled=FALSE,updated_at=CURRENT_TIMESTAMP WHERE enabled=TRUE AND failure_count>?", threshold);
    }

    private RowMapper<Source> rowMapper() {
        return (resultSet, rowNum) -> map(resultSet);
    }

    private Source map(ResultSet resultSet) throws SQLException {
        Source source = new Source();
        source.id = resultSet.getLong("id");
        source.name = resultSet.getString("name");
        source.sourceType = SourceType.valueOf(resultSet.getString("source_type"));
        source.baseUrl = resultSet.getString("base_url");
        source.feedUrl = resultSet.getString("feed_url");
        source.priority = resultSet.getInt("priority");
        source.language = resultSet.getString("language");
        source.enabled = resultSet.getBoolean("enabled");
        source.lastCollectTime = toLocalDateTime(resultSet.getTimestamp("last_collect_time"));
        source.collectorType = resultSet.getString("collector_type");
        source.collectConfig = resultSet.getString("collect_config");
        source.lastSuccessTime = toLocalDateTime(resultSet.getTimestamp("last_success_time"));
        source.lastErrorMessage = resultSet.getString("last_error_message");
        source.failureCount = resultSet.getInt("failure_count");
        source.lastCollectCount = resultSet.getInt("last_collect_count");
        source.authorityLevel = resultSet.getInt("authority_level");
        source.official = resultSet.getBoolean("is_official");
        source.createdAt = toLocalDateTime(resultSet.getTimestamp("created_at"));
        source.updatedAt = toLocalDateTime(resultSet.getTimestamp("updated_at"));
        return source;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
