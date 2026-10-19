package com.aiintelligence.collector;

import com.aiintelligence.article.Article;
import com.aiintelligence.article.ArticleStatus;
import com.aiintelligence.event.ArticleProcessingService;
import com.aiintelligence.source.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class CollectorService {
    private final List<ArticleCollector> collectors;private final SourceService sources;private final ArticleIngestService ingest;private final ArticleProcessingService processing;
    public CollectorService(List<ArticleCollector> collectors,SourceService sources,ArticleIngestService ingest,ArticleProcessingService processing){this.collectors=collectors;this.sources=sources;this.ingest=ingest;this.processing=processing;}
    public Map<Long,Integer> collectAll(){Map<Long,Integer> r=new LinkedHashMap<>();for(Source s:sources.findEnabled())try{r.put(s.id,collect(s.id));}catch(Exception e){r.put(s.id,-1);}return r;}
    public int collect(long sourceId){Source s=sources.findById(sourceId);ArticleCollector c=collectors.stream().filter(x->x.supports(s)).findFirst().orElseThrow(()->new IllegalArgumentException("No collector for source: "+s.name));try{CollectResult result=c.collect(s);int count=0;for(ArticleCandidate candidate:result.articles){candidate.sourceId=s.id;Article article=ingest.ingest(candidate);if(article.collectStatus==ArticleStatus.NEW){count++;try{processing.process(article.id,false);}catch(Exception ignored){}}}sources.collectSucceeded(s.id,count);return count;}catch(Exception e){sources.collectFailed(s.id,e.getMessage());throw e;}}
    public Article importUrl(long sourceId,String url){Source s=sources.findById(sourceId);s.feedUrl=url;s.collectorType="HTTP";CollectResult r=collectors.stream().filter(x->x instanceof HttpArticleCollector).findFirst().get().collect(s);Article article=ingest.ingest(r.articles.get(0));if(article.collectStatus==ArticleStatus.NEW)processing.process(article.id,false);return article;}
}
