package com.aiintelligence.event;

import java.time.LocalDateTime;
import java.util.List;

public class Event {
    public Long id;
    public String title;
    public String summary;
    public String whatHappened;
    public String whyImportant;
    public String availability;
    public String developerImpact;
    public EventCategory category;
    public String status;
    public Integer impactScore;
    public Integer technicalScore;
    public Integer availabilityScore;
    public Integer developerValueScore;
    public Integer credibilityScore;
    public Integer freshnessScore;
    public Integer totalScore;
    public LocalDateTime eventTime;
    public ReviewStatus reviewStatus;
    public PublishStatus publishStatus;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime publishedAt;
    public List<EventArticle> articles;
    public List<EventProduct> products;
    public com.aiintelligence.reader.ReaderArticle readerArticle;
}
