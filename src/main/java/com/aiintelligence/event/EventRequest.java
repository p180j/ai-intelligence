package com.aiintelligence.event;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;

public class EventRequest {
    @NotBlank public String title;
    public String summary;
    public String whatHappened;
    public String whyImportant;
    public String availability;
    public String developerImpact;
    public EventCategory category = EventCategory.NORMAL;
    public String status;
    public Integer impactScore;
    public Integer technicalScore;
    public Integer availabilityScore;
    public Integer developerValueScore;
    public Integer credibilityScore;
    public Integer freshnessScore;
    public Integer totalScore;
    public LocalDateTime eventTime;
    public ReviewStatus reviewStatus = ReviewStatus.PENDING;
    public PublishStatus publishStatus = PublishStatus.DRAFT;
    public LocalDateTime publishedAt;
}

