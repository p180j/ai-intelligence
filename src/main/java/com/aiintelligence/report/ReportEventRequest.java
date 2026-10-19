package com.aiintelligence.report;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ReportEventRequest {
    @NotNull public Long eventId;
    @NotBlank public String section;
    @NotNull public Integer displayOrder;
}
