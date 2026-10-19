package com.aiintelligence.reader;

import com.aiintelligence.ai.*;
import com.aiintelligence.event.*;
import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReaderArticleService {
    private static final String VERSION="reader-v3";
    private final ReaderArticleRepository repository;private final EventService events;private final AiClient ai;private final PromptCatalog prompts;private final ObjectMapper mapper;private final com.aiintelligence.event.PipelineRepository logs;
    public ReaderArticleService(ReaderArticleRepository repository,EventService events,AiClient ai,PromptCatalog prompts,ObjectMapper mapper,com.aiintelligence.event.PipelineRepository logs){this.repository=repository;this.events=events;this.ai=ai;this.prompts=prompts;this.mapper=mapper;this.logs=logs;}
    public ReaderArticle find(long eventId){return repository.find(eventId).orElse(null);}
    @Transactional public ReaderArticle generate(long eventId){Event event=events.findById(eventId);if(event.reviewStatus!=ReviewStatus.APPROVED)throw new IllegalArgumentException("Reader article requires APPROVED event");AiRequest request=new AiRequest();request.requestType="READER_ARTICLE";request.eventId=eventId;request.promptVersion=VERSION;request.systemPrompt=prompts.readerArticle();try{request.userPrompt=mapper.writeValueAsString(event);AiResponse response=ai.generate(request);JsonNode json=mapper.readTree(response.content);String title=required(json,"readerTitle"),lead=required(json,"readerLead"),content=sectioned(required(json,"readerContent")),conclusion=required(json,"readerConclusion");repository.save(eventId,clean(title),clean(lead),clean(content),clean(conclusion),VERSION);logs.log(request,response,"SUCCESS",null);return find(eventId);}catch(Exception e){logs.log(request,null,"FAILED",e.getMessage());throw new IllegalStateException("Reader article generation failed",e);}}
    private String required(JsonNode node,String field){String value=node.path(field).asText(null);if(value==null||value.trim().isEmpty())throw new IllegalArgumentException("AI field missing: "+field);return value;}
    private String sectioned(String content){String value=clean(content);if(!value.startsWith("## ")&&!value.contains("\n## "))throw new IllegalArgumentException("AI field readerContent must contain editorial sections");return value;}
    public String clean(String value){return value==null?null:value.replace("（已由管理员复核）","").replace("已由管理员复核","").trim();}
}
