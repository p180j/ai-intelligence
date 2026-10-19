package com.aiintelligence.article;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepository {
    private static final String SELECT = "SELECT a.*, s.name AS source_name, s.is_official AS source_official, s.authority_level AS source_authority_level FROM ai_article a JOIN ai_source s ON s.id=a.source_id";
    private final JdbcTemplate jdbcTemplate;

    public ArticleRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public Optional<Article> findById(long id) {
        return jdbcTemplate.query(SELECT + " WHERE a.id=?", rowMapper(), id).stream().findFirst();
    }
    public Optional<Article> findByUrl(String url) { return jdbcTemplate.query(SELECT + " WHERE a.url=?", rowMapper(), url).stream().findFirst(); }
    public Optional<Article> findByContentHash(String hash) { return jdbcTemplate.query(SELECT + " WHERE a.content_hash=?", rowMapper(), hash).stream().findFirst(); }
    public void updateStatus(long id, ArticleStatus status) { jdbcTemplate.update("UPDATE ai_article SET collect_status=?,updated_at=CURRENT_TIMESTAMP WHERE id=?", status.name(), id); }

    public List<Article> search(Long sourceId, ArticleStatus status, LocalDateTime publishedAt, String keyword) {
        StringBuilder sql = new StringBuilder(SELECT + " WHERE 1=1");
        List<Object> arguments = new ArrayList<>();
        if (sourceId != null) { sql.append(" AND a.source_id=?"); arguments.add(sourceId); }
        if (status != null) { sql.append(" AND a.collect_status=?"); arguments.add(status.name()); }
        if (publishedAt != null) { sql.append(" AND a.published_at>=?"); arguments.add(Timestamp.valueOf(publishedAt)); }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(a.title) LIKE ? OR LOWER(a.clean_content) LIKE ?)");
            String value = "%" + keyword.toLowerCase() + "%";
            arguments.add(value); arguments.add(value);
        }
        sql.append(" ORDER BY COALESCE(a.published_at, a.created_at) DESC, a.id DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper(), arguments.toArray());
    }

    public long insert(ArticleRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ai_article (source_id,title,url,author,language,published_at,raw_content,clean_content,content_hash,collect_status,collected_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    new String[]{"id"});
            statement.setLong(1, request.sourceId);
            statement.setString(2, request.title);
            statement.setString(3, request.url);
            statement.setString(4, request.author);
            statement.setString(5, request.language);
            statement.setTimestamp(6, timestamp(request.publishedAt));
            statement.setString(7, request.rawContent);
            statement.setString(8, request.cleanContent);
            statement.setString(9, request.contentHash);
            statement.setString(10, request.collectStatus == null ? ArticleStatus.NEW.name() : request.collectStatus.name());
            statement.setTimestamp(11, timestamp(request.collectedAt));
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private RowMapper<Article> rowMapper() { return (resultSet, rowNum) -> map(resultSet); }

    private Article map(ResultSet rs) throws SQLException {
        Article article = new Article();
        article.id = rs.getLong("id"); article.sourceId = rs.getLong("source_id"); article.sourceName = rs.getString("source_name");
        article.sourceOfficial = rs.getBoolean("source_official"); article.sourceAuthorityLevel = rs.getInt("source_authority_level");
        article.title = rs.getString("title"); article.url = rs.getString("url"); article.author = rs.getString("author");
        article.language = rs.getString("language"); article.publishedAt = local(rs.getTimestamp("published_at"));
        article.rawContent = rs.getString("raw_content"); article.cleanContent = rs.getString("clean_content");
        article.contentHash = rs.getString("content_hash"); article.collectStatus = ArticleStatus.valueOf(rs.getString("collect_status"));
        article.collectedAt = local(rs.getTimestamp("collected_at")); article.createdAt = local(rs.getTimestamp("created_at"));
        article.updatedAt = local(rs.getTimestamp("updated_at"));
        return article;
    }

    private Timestamp timestamp(LocalDateTime value) { return value == null ? null : Timestamp.valueOf(value); }
    private LocalDateTime local(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }
}
