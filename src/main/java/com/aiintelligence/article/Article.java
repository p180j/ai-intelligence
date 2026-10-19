package com.aiintelligence.article;

import java.time.LocalDateTime;

public class Article {
    public Long id;
    public Long sourceId;
    public String sourceName;
    public Boolean sourceOfficial;
    public Integer sourceAuthorityLevel;
    public String title;
    public String url;
    public String author;
    public String language;
    public LocalDateTime publishedAt;
    public String rawContent;
    public String cleanContent;
    public String contentHash;
    public ArticleStatus collectStatus;
    public LocalDateTime collectedAt;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
