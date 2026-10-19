package com.aiintelligence.source;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SourceRequest {
    @NotBlank public String name;
    @NotNull public SourceType sourceType;
    public String baseUrl;
    public String feedUrl;
    public Integer priority = 0;
    public String language;
    public Boolean enabled = true;
    public LocalDateTime lastCollectTime;
    public String collectorType;
    public String collectConfig;
    public Integer authorityLevel = 5;
    public Boolean official = false;
}
