package com.aiintelligence.source;

import java.time.LocalDateTime;

public class Source {
    public Long id;
    public String name;
    public SourceType sourceType;
    public String baseUrl;
    public String feedUrl;
    public Integer priority;
    public String language;
    public Boolean enabled;
    public LocalDateTime lastCollectTime;
    public String collectorType;
    public String collectConfig;
    public LocalDateTime lastSuccessTime;
    public String lastErrorMessage;
    public Integer failureCount;
    public Integer lastCollectCount;
    public Integer authorityLevel;
    public Boolean official;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
