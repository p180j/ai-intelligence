package com.aiintelligence.web;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbc;public DashboardController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping public Map<String,Object> dashboard(){return jdbc.queryForMap("SELECT (SELECT COUNT(*) FROM ai_article WHERE created_at>=CURRENT_DATE) todayArticles,(SELECT COUNT(*) FROM ai_event WHERE created_at>=CURRENT_DATE) todayEvents,(SELECT COUNT(*) FROM ai_event WHERE review_status='PENDING') pendingEvents,(SELECT COUNT(*) FROM ai_event WHERE review_status='APPROVED' AND updated_at>=CURRENT_DATE) todayApprovedEvents,(SELECT COUNT(*) FROM ai_event WHERE review_status='APPROVED' AND publish_status='PUBLISHED') publishedEvents,(SELECT COUNT(*) FROM ai_event WHERE review_status='APPROVED' AND publish_status='PUBLISHED' AND published_at>=CURRENT_DATE) todayPublishedEvents,(SELECT COUNT(*) FROM ai_article WHERE collect_status='AI_FAILED') aiFailedArticles,(SELECT COUNT(*) FROM ai_source WHERE failure_count>0) failedSources");}
    @GetMapping("/published-events") public List<Map<String,Object>> published(){return jdbc.query("SELECT e.id,e.title,e.category,e.event_time,e.published_at,GROUP_CONCAT(DISTINCT p.name ORDER BY p.name SEPARATOR '、') products,GROUP_CONCAT(DISTINCT r.title ORDER BY r.report_date DESC SEPARATOR '、') reports FROM ai_event e LEFT JOIN ai_product_event pe ON pe.event_id=e.id LEFT JOIN ai_product p ON p.id=pe.product_id LEFT JOIN ai_daily_report_event dre ON dre.event_id=e.id LEFT JOIN ai_daily_report r ON r.id=dre.report_id WHERE e.review_status='APPROVED' AND e.publish_status='PUBLISHED' GROUP BY e.id,e.title,e.category,e.event_time,e.published_at ORDER BY e.published_at DESC,e.id DESC",(rs,n)->{Map<String,Object> item=new LinkedHashMap<>();item.put("id",rs.getLong("id"));item.put("title",rs.getString("title"));item.put("category",rs.getString("category"));item.put("products",rs.getString("products"));item.put("eventTime",rs.getTimestamp("event_time"));item.put("publishedAt",rs.getTimestamp("published_at"));item.put("reports",rs.getString("reports"));item.put("publicUrl","/event/"+rs.getLong("id"));return item;});}
}
