package com.aiintelligence.report;

import com.aiintelligence.event.Event;
import com.aiintelligence.event.EventRepository;
import java.sql.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Repository;

@Repository
public class DailyReportRepository {
    private final JdbcTemplate jdbc; private final EventRepository events;
    public DailyReportRepository(JdbcTemplate jdbc,EventRepository events){this.jdbc=jdbc;this.events=events;}
    public List<DailyReport> findAll(){return jdbc.query("SELECT * FROM ai_daily_report ORDER BY report_date DESC",(r,n)->map(r));}
    public Optional<DailyReport> findById(long id){return jdbc.query("SELECT * FROM ai_daily_report WHERE id=?",(r,n)->map(r),id).stream().findFirst();}
    public Optional<DailyReport> findByDate(LocalDate date){return jdbc.query("SELECT * FROM ai_daily_report WHERE report_date=?",(r,n)->map(r),java.sql.Date.valueOf(date)).stream().findFirst();}
    public Optional<DailyReport> findPublishedLatest(){return jdbc.query("SELECT * FROM ai_daily_report WHERE status='PUBLISHED' ORDER BY report_date DESC LIMIT 1",(r,n)->map(r)).stream().findFirst();}
    public List<Event> findAllPublished(){return jdbc.query("SELECT * FROM ai_event WHERE review_status='APPROVED' AND publish_status='PUBLISHED' ORDER BY published_at DESC,id DESC",events.rowMapperForReuse());}
    public long insert(LocalDate date,String title,String summary){KeyHolder key=new GeneratedKeyHolder();jdbc.update(c->{PreparedStatement s=c.prepareStatement("INSERT INTO ai_daily_report(report_date,title,summary,status) VALUES(?,?,?,'DRAFT')",new String[]{"id"});s.setDate(1,java.sql.Date.valueOf(date));s.setString(2,title);s.setString(3,summary);return s;},key);return key.getKey().longValue();}
    public void update(long id,String title,String summary){jdbc.update("UPDATE ai_daily_report SET title=?,summary=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",title,summary,id);}
    public void publish(long id){jdbc.update("UPDATE ai_daily_report SET status='PUBLISHED',published_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE id=?",id);}
    public void offline(long id){jdbc.update("UPDATE ai_daily_report SET status='OFFLINE',updated_at=CURRENT_TIMESTAMP WHERE id=?",id);}
    public void addEvent(long reportId,long eventId,String section,int order){int n=jdbc.update("UPDATE ai_daily_report_event SET section=?,display_order=? WHERE report_id=? AND event_id=?",section,order,reportId,eventId);if(n==0)jdbc.update("INSERT INTO ai_daily_report_event(report_id,event_id,section,display_order) VALUES(?,?,?,?)",reportId,eventId,section,order);}
    public void removeEvent(long reportId,long eventId){jdbc.update("DELETE FROM ai_daily_report_event WHERE report_id=? AND event_id=?",reportId,eventId);}
    public List<DailyReportEvent> findEvents(long reportId){return jdbc.query("SELECT event_id,section,display_order,quality_feedback FROM ai_daily_report_event WHERE report_id=? ORDER BY display_order,id",(r,n)->mapEvent(r),reportId);}
    public List<DailyReportEvent> findPublishedEvents(long reportId){return jdbc.query("SELECT re.event_id,re.section,re.display_order,re.quality_feedback FROM ai_daily_report_event re JOIN ai_event e ON e.id=re.event_id WHERE re.report_id=? AND e.review_status='APPROVED' AND e.publish_status='PUBLISHED' ORDER BY re.display_order,re.id",(r,n)->mapEvent(r),reportId);}
    public void feedback(long reportId,long eventId,String feedback){if(jdbc.update("UPDATE ai_daily_report_event SET quality_feedback=? WHERE report_id=? AND event_id=?",feedback,reportId,eventId)==0)throw new com.aiintelligence.common.NotFoundException("Report event not found");}
    public List<Event> candidates(LocalDate date){Timestamp start=Timestamp.valueOf(date.atStartOfDay());Timestamp end=Timestamp.valueOf(date.plusDays(1).atStartOfDay());return jdbc.query("SELECT e.* FROM ai_event e WHERE e.review_status='APPROVED' AND e.publish_status IN ('DRAFT','PUBLISHED') AND e.category<>'NORMAL' AND COALESCE(e.event_time,e.created_at)>=? AND COALESCE(e.event_time,e.created_at)<? AND NOT EXISTS(SELECT 1 FROM ai_daily_report_event re JOIN ai_daily_report r ON r.id=re.report_id WHERE re.event_id=e.id AND r.report_date=?) ORDER BY e.total_score DESC,COALESCE(e.event_time,e.created_at) DESC",events.rowMapperForReuse(),start,end,java.sql.Date.valueOf(date));}
    private DailyReportEvent mapEvent(ResultSet r)throws SQLException{DailyReportEvent d=new DailyReportEvent();d.eventId=r.getLong("event_id");d.section=r.getString("section");d.displayOrder=r.getInt("display_order");d.qualityFeedback=r.getString("quality_feedback");Event e=events.findById(d.eventId).orElse(null);if(e!=null){e.articles=events.findArticles(e.id);e.products=events.findProducts(e.id);}d.event=e;return d;}
    private DailyReport map(ResultSet r)throws SQLException{DailyReport d=new DailyReport();d.id=r.getLong("id");d.reportDate=r.getDate("report_date").toLocalDate();d.title=r.getString("title");d.summary=r.getString("summary");d.status=DailyReportStatus.valueOf(r.getString("status"));d.createdAt=local(r.getTimestamp("created_at"));d.updatedAt=local(r.getTimestamp("updated_at"));d.publishedAt=local(r.getTimestamp("published_at"));return d;}
    private LocalDateTime local(Timestamp t){return t==null?null:t.toLocalDateTime();}
}
