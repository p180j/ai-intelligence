package com.aiintelligence.event;

import com.aiintelligence.ai.*;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PipelineRepository {
    private final JdbcTemplate jdbc;public PipelineRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public void log(AiRequest r,AiResponse a,String status,String error){jdbc.update("INSERT INTO ai_processing_log(article_id,event_id,request_type,ai_provider,ai_model,prompt_version,status,input_tokens,output_tokens,latency_ms,error_message) VALUES(?,?,?,?,?,?,?,?,?,?,?)",r.articleId,r.eventId,r.requestType,a==null?"unknown":a.provider,a==null?"unknown":a.model,r.promptVersion,status,a==null?null:a.inputTokens,a==null?null:a.outputTokens,a==null?null:a.latencyMs,error);}
    public void linkProductOrCandidate(long eventId,String name,String type){java.util.List<Long> ids=jdbc.query("SELECT id FROM ai_product WHERE LOWER(name)=LOWER(?) OR LOWER(slug)=LOWER(?)",(rs,n)->rs.getLong(1),name,name.toLowerCase().replace(' ','-'));if(ids.isEmpty()){int n=jdbc.update("UPDATE ai_product_candidate SET entity_type=? WHERE event_id=? AND name=?",type,eventId,name);if(n==0)jdbc.update("INSERT INTO ai_product_candidate(event_id,name,entity_type) VALUES(?,?,?)",eventId,name,type);}else{int n=jdbc.update("UPDATE ai_product_event SET relation_type='MENTIONED' WHERE product_id=? AND event_id=?",ids.get(0),eventId);if(n==0)jdbc.update("INSERT INTO ai_product_event(product_id,event_id,relation_type) VALUES(?,?,'MENTIONED')",ids.get(0),eventId);}}
}
