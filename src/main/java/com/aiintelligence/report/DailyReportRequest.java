package com.aiintelligence.report;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;

public class DailyReportRequest {
    @NotNull public LocalDate reportDate;
}
