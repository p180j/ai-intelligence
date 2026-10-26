package com.aiintelligence.event;

import com.aiintelligence.ai.*;
import java.text.Normalizer;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PipelineRepository {
    private final JdbcTemplate jdbc;public PipelineRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public void log(AiRequest r,AiResponse a,String status,String error){jdbc.update("INSERT INTO ai_processing_log(article_id,event_id,request_type,ai_provider,ai_model,prompt_version,status,input_tokens,output_tokens,latency_ms,error_message) VALUES(?,?,?,?,?,?,?,?,?,?,?)",r.articleId,r.eventId,r.requestType,a==null?"unknown":a.provider,a==null?"unknown":a.model,r.promptVersion,status,a==null?null:a.inputTokens,a==null?null:a.outputTokens,a==null?null:a.latencyMs,error);}
    public boolean linkProductIfKnown(long eventId,String name){Long productId=matchingProductId(name);if(productId==null)return false;attach(productId,eventId);return true;}
    public int reconcileProductCandidates(){List<Object[]> candidates=jdbc.query("SELECT id,event_id,name FROM ai_product_candidate WHERE status='PENDING' ORDER BY id",(rs,n)->new Object[]{rs.getLong("id"),rs.getLong("event_id"),rs.getString("name")});int matched=0;for(Object[] candidate:candidates)if(linkProductIfKnown((Long)candidate[1],(String)candidate[2])){jdbc.update("UPDATE ai_product_candidate SET status='CONFIRMED' WHERE id=?",candidate[0]);matched++;}return matched;}
    private Long matchingProductId(String name){String candidate=normalize(name);if(candidate.isEmpty())return null;List<Object[]> products=jdbc.query("SELECT id,name,slug FROM ai_product",(rs,n)->new Object[]{rs.getLong("id"),rs.getString("name"),rs.getString("slug")});Long bestId=null;int bestScore=-1;for(Object[] product:products){String productName=normalize((String)product[1]),slug=normalize((String)product[2]);int score=Math.max(score(candidate,productName),score(candidate,slug));if(score>bestScore){bestScore=score;bestId=(Long)product[0];}}return bestScore<0?null:bestId;}
    private int score(String candidate,String product){if(product.isEmpty())return -1;if(candidate.equals(product))return 10000+product.length();if(!candidate.startsWith(product+" "))return -1;String suffix=candidate.substring(product.length()+1);return variantSuffix(suffix)?1000+product.length():-1;}
    private boolean variantSuffix(String suffix){return suffix.matches("v?\\d.*")||suffix.startsWith("for ")||suffix.startsWith("cli")||suffix.startsWith("app")||suffix.startsWith("desktop")||suffix.startsWith("web")||suffix.startsWith("plugin")||suffix.startsWith("extension")||suffix.startsWith("插件")||suffix.startsWith("客户端")||suffix.startsWith("扩展");}
    private String normalize(String value){return value==null?"":Normalizer.normalize(value,Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim().replaceAll("\\s+"," ");}
    private void attach(long productId,long eventId){int n=jdbc.update("UPDATE ai_product_event SET relation_type='MENTIONED' WHERE product_id=? AND event_id=?",productId,eventId);if(n==0)jdbc.update("INSERT INTO ai_product_event(product_id,event_id,relation_type) VALUES(?,?,'MENTIONED')",productId,eventId);}
}
