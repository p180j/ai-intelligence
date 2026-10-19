package com.aiintelligence.report;

import javax.validation.constraints.NotBlank;

public class DailyReportUpdateRequest {
    @NotBlank public String title;
    public String summary;
}
