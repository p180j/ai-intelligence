package com.aiintelligence.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DailyReport {
    public Long id;
    public LocalDate reportDate;
    public String title;
    public String summary;
    public DailyReportStatus status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime publishedAt;
    public List<DailyReportEvent> events;
}
