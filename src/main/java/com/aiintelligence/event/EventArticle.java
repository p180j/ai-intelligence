package com.aiintelligence.event;

import java.time.LocalDateTime;

public class EventArticle {
    public Long articleId;
    public Long sourceId;
    public String sourceName;
    public String title;
    public String url;
    public LocalDateTime publishedAt;
    public ArticleRelationType relationType;
    public Boolean primarySource;
}

