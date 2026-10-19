package com.aiintelligence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aiintelligence.collector.*;
import com.aiintelligence.event.ArticleProcessingService;
import com.aiintelligence.source.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class CollectorIsolationTest {
    @Test void oneSourceFailureDoesNotBlockOtherSources(){SourceService sources=mock(SourceService.class);ArticleIngestService ingest=mock(ArticleIngestService.class);ArticleProcessingService processing=mock(ArticleProcessingService.class);Source good=source(1,"good"),bad=source(2,"bad");when(sources.findEnabled()).thenReturn(Arrays.asList(good,bad));when(sources.findById(1)).thenReturn(good);when(sources.findById(2)).thenReturn(bad);ArticleCollector collector=new ArticleCollector(){public boolean supports(Source s){return true;}public CollectResult collect(Source s){if(s.id==2)throw new IllegalStateException("source unavailable");return new CollectResult();}};CollectorService service=new CollectorService(Collections.singletonList(collector),sources,ingest,processing);Map<Long,Integer> result=service.collectAll();assertEquals(0,result.get(1L));assertEquals(-1,result.get(2L));verify(sources).collectSucceeded(1,0);verify(sources).collectFailed(2,"source unavailable");}
    private Source source(long id,String name){Source s=new Source();s.id=id;s.name=name;s.sourceType=SourceType.MANUAL;s.enabled=true;return s;}
}
