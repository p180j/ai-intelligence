package com.aiintelligence;

import static org.junit.jupiter.api.Assertions.*;

import com.aiintelligence.article.*;
import com.aiintelligence.event.*;
import com.aiintelligence.quality.*;
import com.aiintelligence.report.*;
import com.aiintelligence.source.*;
import java.time.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest @DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Phase6IntegrationTest {
    @Autowired SourceService sources;@Autowired ArticleService articles;@Autowired EventService events;@Autowired QualityService quality;@Autowired DailyReportService reports;
    @Test void qualityFeedbackCreatesEvalCasesAndDailyStats(){SourceRequest sr=new SourceRequest();sr.name="Eval Source";sr.sourceType=SourceType.MANUAL;Source source=sources.create(sr);Article a=article(source.id,"同一错误合并 A","https://eval.test/a"),b=article(source.id,"同一错误合并 B","https://eval.test/b");Event event=event("错误合并 Event",EventCategory.MUST_READ);attach(event.id,a.id);attach(event.id,b.id);EventFeedbackRequest feedback=new EventFeedbackRequest();feedback.qualityResult=EventQualityResult.MAJOR_EDIT;feedback.wrongMerge=true;feedback.articleAId=a.id;feedback.articleBId=b.id;feedback.reviewSeconds=180;feedback.notes="应该拆成两个事件";quality.save(event.id,feedback);assertEquals(EventQualityResult.MAJOR_EDIT,quality.find(event.id).qualityResult);assertEquals(MatchExpectation.DIFFERENT_EVENT,quality.evalCases().get(0).expectedResult);Map<String,Object> stats=quality.stats(LocalDate.now());assertEquals(1L,((Number)stats.get("wrongMergeCount")).longValue());assertEquals(180L,((Number)stats.get("reviewSeconds")).longValue());}
    @Test void reportQualityControlsWechatCopy(){LocalDate day=LocalDate.now().plusDays(10);Event event=event("值得发布的工具",EventCategory.TRY_NOW);EventRequest moved=request("值得发布的工具",EventCategory.TRY_NOW);moved.eventTime=day.atTime(9,0);events.update(event.id,moved);events.review(event.id,ReviewStatus.APPROVED);DailyReport report=reports.generate(day);reports.feedback(report.id,event.id,ReportQualityResult.MUST_SEND.name());assertTrue(reports.wechat(report.id).contains("值得发布的工具"));reports.feedback(report.id,event.id,ReportQualityResult.NOT_WORTHY.name());assertFalse(reports.wechat(report.id).contains("值得发布的工具"));}
    private Article article(long sourceId,String title,String url){ArticleRequest r=new ArticleRequest();r.sourceId=sourceId;r.title=title;r.url=url;r.cleanContent="用于 Eval 的完整测试内容，长度足够且包含明确的事件事实。";return articles.create(r);}
    private Event event(String title,EventCategory category){return events.create(request(title,category));}
    private EventRequest request(String title,EventCategory category){EventRequest r=new EventRequest();r.title=title;r.summary="摘要";r.whyImportant="重要";r.category=category;r.eventTime=LocalDateTime.now();r.totalScore=8;return r;}
    private void attach(long eventId,long articleId){ArticleRelationRequest r=new ArticleRelationRequest();r.relationType=ArticleRelationType.SUPPORTING;events.attachArticle(eventId,articleId,r);}
}
