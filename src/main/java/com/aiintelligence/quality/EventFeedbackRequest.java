package com.aiintelligence.quality;

import javax.validation.constraints.NotNull;

public class EventFeedbackRequest {
    @NotNull public EventQualityResult qualityResult;
    public Boolean wrongMerge=false;
    public Long articleAId;
    public Long articleBId;
    public Long duplicateEventId;
    public Integer reviewSeconds;
    public String notes;
}
