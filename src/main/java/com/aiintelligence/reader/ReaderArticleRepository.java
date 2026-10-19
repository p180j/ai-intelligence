package com.aiintelligence.reader;

import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReaderArticleRepository {
    private final JdbcTemplate jdbc;
    public ReaderArticleRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<ReaderArticle> find(long eventId){return jdbc.query("SELECT * FROM ai_event_reader_article WHERE event_id=?",(r,n)->{ReaderArticle a=new ReaderArticle();a.eventId=r.getLong("event_id");a.readerTitle=r.getString("reader_title");a.readerLead=r.getString("reader_lead");a.readerContent=r.getString("reader_content");a.readerConclusion=r.getString("reader_conclusion");Timestamp generated=r.getTimestamp("generated_at");a.generatedAt=generated==null?null:generated.toLocalDateTime();a.promptVersion=r.getString("prompt_version");return a;},eventId).stream().findFirst();}
    public void save(long eventId,String title,String lead,String content,String conclusion,String version){int updated=jdbc.update("UPDATE ai_event_reader_article SET reader_title=?,reader_lead=?,reader_content=?,reader_conclusion=?,generated_at=CURRENT_TIMESTAMP,prompt_version=? WHERE event_id=?",title,lead,content,conclusion,version,eventId);if(updated==0)jdbc.update("INSERT INTO ai_event_reader_article(event_id,reader_title,reader_lead,reader_content,reader_conclusion,prompt_version) VALUES(?,?,?,?,?,?)",eventId,title,lead,content,conclusion,version);}
}
