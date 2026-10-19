package com.aiintelligence.web;

import com.aiintelligence.event.*;
import com.aiintelligence.product.*;
import com.aiintelligence.report.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/public")
public class PublicController {
    private final DailyReportService reports;private final EventService events;private final ProductService products;private final JdbcTemplate jdbc;private final com.aiintelligence.reader.ReaderArticleService readers;
    public PublicController(DailyReportService reports,EventService events,ProductService products,JdbcTemplate jdbc,com.aiintelligence.reader.ReaderArticleService readers){this.reports=reports;this.events=events;this.products=products;this.jdbc=jdbc;this.readers=readers;}
    @GetMapping("/report/latest") public DailyReport latest(HttpServletRequest request){DailyReport report=reports.latestPublished();if(report!=null&&report.events!=null)report.events.forEach(item->decorate(item.event));view("HOME",report==null?null:report.id,null,null,request);return report;}
    @GetMapping("/events/{id}") public Event event(@PathVariable long id,HttpServletRequest request){Event e=events.findById(id);if(e.reviewStatus!=ReviewStatus.APPROVED||e.publishStatus!=PublishStatus.PUBLISHED)throw new com.aiintelligence.common.NotFoundException("Event not published: "+id);decorate(e);view("EVENT",null,id,null,request);return e;}
    @GetMapping("/events/{id}/navigation") public Map<String,Object> navigation(@PathVariable long id){Map<String,Object> result=new LinkedHashMap<>();result.put("previous",jdbc.query("SELECT id,title FROM ai_event WHERE review_status='APPROVED' AND publish_status='PUBLISHED' AND id<>? AND COALESCE(event_time,created_at)<(SELECT COALESCE(event_time,created_at) FROM ai_event WHERE id=?) ORDER BY COALESCE(event_time,created_at) DESC LIMIT 1",(r,n)->mapLink(r),id,id).stream().findFirst().orElse(null));result.put("next",jdbc.query("SELECT id,title FROM ai_event WHERE review_status='APPROVED' AND publish_status='PUBLISHED' AND id<>? AND COALESCE(event_time,created_at)>(SELECT COALESCE(event_time,created_at) FROM ai_event WHERE id=?) ORDER BY COALESCE(event_time,created_at) LIMIT 1",(r,n)->mapLink(r),id,id).stream().findFirst().orElse(null));return result;}
    @GetMapping("/products/{slug}") public Product product(@PathVariable String slug,HttpServletRequest request){Product p=products.findPublishedBySlug(slug);view("PRODUCT",null,null,p.id,request);return p;}
    @GetMapping("/search") public Map<String,Object> search(@RequestParam(defaultValue="")String q){Map<String,Object> result=new LinkedHashMap<>();List<Event> published=events.searchPublished(q);published.forEach(this::decorate);result.put("events",published);result.put("products",products.search(q));return result;}
    private void view(String type,Long reportId,Long eventId,Long productId,HttpServletRequest r){jdbc.update("INSERT INTO ai_page_view(page_type,report_id,event_id,product_id,referrer,user_agent) VALUES(?,?,?,?,?,?)",type,reportId,eventId,productId,limit(r.getHeader("Referer")),limit(r.getHeader("User-Agent")));}
    private String limit(String s){return s==null?null:s.substring(0,Math.min(1000,s.length()));}
    private String clean(String value){return readers.clean(value);}
    private void decorate(Event e){if(e==null)return;e.title=clean(e.title);e.summary=clean(e.summary);e.whatHappened=clean(e.whatHappened);e.whyImportant=clean(e.whyImportant);e.availability=clean(e.availability);e.developerImpact=clean(e.developerImpact);e.readerArticle=readers.find(e.id);}
    private Map<String,Object> mapLink(java.sql.ResultSet r)throws java.sql.SQLException{Map<String,Object> link=new LinkedHashMap<>();link.put("id",r.getLong("id"));link.put("title",clean(r.getString("title")));return link;}
}
