package com.aiintelligence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aiintelligence.ai.*;
import com.aiintelligence.article.*;
import com.aiintelligence.collector.*;
import com.aiintelligence.event.*;
import com.aiintelligence.source.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest @DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Phase23IntegrationTest {
    @Autowired SourceService sources;@Autowired ArticleIngestService ingest;@Autowired ArticleProcessingService processing;@Autowired EventService events;@Autowired RssArticleCollector rss;
    @MockBean AiClient ai;

    @BeforeEach void fakeAi(){when(ai.generate(any())).thenAnswer(invocation->{AiRequest r=invocation.getArgument(0);AiResponse a=new AiResponse();a.provider="fake";a.model="fake-v1";a.latencyMs=1;if("ARTICLE_ANALYSIS".equals(r.requestType)){String title=value(r.userPrompt,"title");String candidate=title.contains("融资")?"Hermes 宣布融资":title.contains("v1.3")?"Hermes 发布 v1.3":title.contains("v1.2")?"Hermes 发布 v1.2":"Claude Code 发布功能 X";a.content="{\"relevant\":true,\"reason\":\"developer news\",\"language\":\"zh\",\"eventType\":\"PRODUCT_RELEASE\",\"entities\":[{\"type\":\"PRODUCT\",\"name\":\""+(title.contains("Hermes")?"Hermes":"Claude Code")+"\"}],\"eventCandidateTitle\":\""+candidate+"\",\"eventTime\":\""+LocalDateTime.now().withNano(0)+"\",\"summary\":\"摘要\"}";}else if("EVENT_MATCHING".equals(r.requestType)){boolean same=r.userPrompt.contains("Claude Code")&&!r.userPrompt.contains("Hermes");Matcher m=Pattern.compile("\\\"id\\\":(\\d+)").matcher(r.userPrompt);long id=m.find()?Long.parseLong(m.group(1)):0;a.content="{\"matched\":"+same+",\"eventId\":"+(same?id:"null")+",\"confidence\":0.96,\"reason\":\"same occurrence\"}";}else{a.content="{\"title\":\"AI 事件\",\"summary\":\"面向开发者的摘要\",\"whatHappened\":\"确认发生了产品更新\",\"whyImportant\":\"影响开发工作流\",\"availability\":\"AVAILABLE\",\"developerImpact\":\"开发者可以立即评估\",\"eventTime\":\""+LocalDateTime.now().withNano(0)+"\",\"category\":\"TRY_NOW\",\"impactScore\":8,\"technicalScore\":8,\"availabilityScore\":9,\"developerValueScore\":9,\"credibilityScore\":7,\"freshnessScore\":10}";}return a;});}

    @Test void rssAndDeterministicDedupWork() throws Exception {Source s=source("RSS Source",false);s.feedUrl=Paths.get(getClass().getResource("/fixtures/feed.xml").toURI()).toUri().toString();s.collectorType="RSS";CollectResult result=rss.collect(s);assertEquals(1,result.articles.size());Article first=ingest.ingest(result.articles.get(0));ArticleCandidate duplicate=result.articles.get(0);duplicate.url="https://example.test/claude-x?utm_campaign=other";Article second=ingest.ingest(duplicate);assertEquals(first.id,second.id);ArticleCandidate sameContent=new ArticleCandidate();sameContent.sourceId=s.id;sameContent.title="Mirror";sameContent.url="https://mirror.test/x";sameContent.rawContent=duplicate.rawContent;assertEquals(first.id,ingest.ingest(sameContent).id);}

    @Test void sameOccurrenceMergesButVersionsAndFundingDoNot(){Source s=source("Media",false);Event first=processing.process(article(s,"Claude Code 发布功能 X","https://a.test/1","Claude Code feature X has been released for developers with detailed technical information.").id,false);Event merged=processing.process(article(s,"Claude Code gets feature X","https://b.test/2","Independent report confirms Claude Code feature X release and developer availability today.").id,false);assertEquals(first.id,merged.id);assertEquals(2,events.findById(first.id).articles.size());processing.process(article(s,"Hermes v1.2","https://a.test/v12","Hermes v1.2 is released with a set of developer improvements and documented changes.").id,false);processing.process(article(s,"Hermes v1.3","https://a.test/v13","Hermes v1.3 is released as a separate version with new documented changes for developers.").id,false);processing.process(article(s,"Hermes 宣布融资","https://a.test/funding","Hermes announced a financing round for company growth, separate from any software release.").id,false);assertEquals(4,events.findAll().size());}

    @Test void invalidJsonRetriesOnceThenMarksAiFailed(){org.mockito.Mockito.reset(ai);AtomicInteger calls=new AtomicInteger();when(ai.generate(any())).thenAnswer(i->{calls.incrementAndGet();AiResponse a=new AiResponse();a.provider="fake";a.model="fake";a.content="not-json";return a;});Source s=source("Broken AI",false);Article article=article(s,"Valid technical title","https://fail.test/1","This is sufficiently long technical AI developer content that must reach the model processing step.");assertThrows(IllegalStateException.class,()->processing.process(article.id,false));assertEquals(2,calls.get());assertEquals(ArticleStatus.AI_FAILED,articles().findById(article.id).collectStatus);}

    private Source source(String name,boolean official){SourceRequest r=new SourceRequest();r.name=name;r.sourceType=SourceType.MANUAL;r.official=official;return sources.create(r);}private Article article(Source s,String title,String url,String content){ArticleCandidate c=new ArticleCandidate();c.sourceId=s.id;c.title=title;c.url=url;c.rawContent=content;c.publishedAt=LocalDateTime.now();return ingest.ingest(c);}private ArticleService articles(){return org.springframework.test.util.ReflectionTestUtils.getField(processing,"articles") instanceof ArticleService?(ArticleService)org.springframework.test.util.ReflectionTestUtils.getField(processing,"articles"):null;}private String value(String json,String field){Matcher m=Pattern.compile("\\\""+field+"\\\":\\\"([^\\\"]+)").matcher(json);return m.find()?m.group(1):"";}
}
