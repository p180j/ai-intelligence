package com.aiintelligence;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aiintelligence.event.*;
import com.aiintelligence.product.*;
import com.aiintelligence.report.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Phase45IntegrationTest {
    @Autowired EventService events;@Autowired ProductService products;@Autowired DailyReportService reports;@Autowired MockMvc mvc;

    @Test void approvedEventsGenerateOrderedReportNormalIsExcludedAndPublishMakesItPublic()throws Exception{
        Event must=event("必须关注的模型发布",EventCategory.MUST_READ,9);Event normal=event("普通动态",EventCategory.NORMAL,10);Event tryNow=event("值得尝试的新工具",EventCategory.TRY_NOW,7);
        events.review(must.id,ReviewStatus.APPROVED);events.review(normal.id,ReviewStatus.APPROVED);events.review(tryNow.id,ReviewStatus.APPROVED);
        ProductRequest request=new ProductRequest();request.name="Test Agent";request.slug="test-agent";request.summary="面向开发者的测试 Agent";Product product=products.create(request);products.attachEvent(product.id,must.id,null);
        DailyReport report=reports.generate(LocalDate.now());assertEquals(2,report.events.size());assertEquals(must.id,report.events.get(0).eventId);assertTrue(report.events.stream().noneMatch(x->x.eventId.equals(normal.id)));assertEquals("MUST_READ",report.events.get(0).section);assertEquals("TRY_NOW",report.events.get(1).section);
        DailyReport published=reports.publish(report.id);assertEquals(DailyReportStatus.PUBLISHED,published.status);assertEquals(PublishStatus.PUBLISHED,events.findById(must.id).publishStatus);
        mvc.perform(get("/api/public/report/latest")).andExpect(status().isOk()).andExpect(jsonPath("$.events[*].event.id",hasItems(must.id.intValue(),tryNow.id.intValue())));
        mvc.perform(get("/api/public/events/{id}",must.id)).andExpect(status().isOk()).andExpect(jsonPath("$.title").value(must.title));
        mvc.perform(get("/api/public/products/test-agent")).andExpect(status().isOk()).andExpect(jsonPath("$.events[0].eventId").value(must.id));
        mvc.perform(get("/api/public/search").param("q","模型")).andExpect(status().isOk()).andExpect(jsonPath("$.events[0].id").value(must.id));
        mvc.perform(get("/api/public/search").param("q","Agent")).andExpect(status().isOk()).andExpect(jsonPath("$.products[0].slug").value("test-agent"));
        events.offline(must.id);
        mvc.perform(get("/api/public/report/latest")).andExpect(status().isOk()).andExpect(jsonPath("$.events[*].event.id",not(hasItem(must.id.intValue()))));
        mvc.perform(get("/api/public/events/{id}",must.id)).andExpect(status().isNotFound());
        events.publish(must.id);
        mvc.perform(get("/api/public/events/{id}",must.id)).andExpect(status().isOk());
    }

    @Test void pendingCanBeEditedAndApprovedAndReportCanBeEdited()throws Exception{
        EventRequest initial=request("AI 初稿",EventCategory.BUSINESS,8);initial.eventTime=LocalDateTime.now().plusDays(1);Event pending=events.create(initial);EventRequest edit=request("人工修改后的标题",EventCategory.BUSINESS,8);edit.eventTime=initial.eventTime;events.update(pending.id,edit);assertEquals(ReviewStatus.PENDING,events.findById(pending.id).reviewStatus);events.review(pending.id,ReviewStatus.APPROVED);
        DailyReport report=reports.generate(LocalDate.now().plusDays(1));DailyReportUpdateRequest update=new DailyReportUpdateRequest();update.title="人工日报标题";update.summary="人工编辑摘要";assertEquals("人工日报标题",reports.update(report.id,update).title);
        assertTrue(reports.wechat(report.id).contains("人工修改后的标题"));
    }

    @Test void directlyPublishedEventAppearsInTodaysPublicDigest()throws Exception{
        EventRequest request=request("今天直接发布的文章",EventCategory.MUST_READ,9);request.eventTime=LocalDateTime.now().minusDays(1);Event published=events.create(request);
        events.review(published.id,ReviewStatus.APPROVED);
        events.publish(published.id);

        mvc.perform(get("/api/public/report/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.title").value("最新 AI 情报"))
                .andExpect(jsonPath("$.events[*].event.id",hasItem(published.id.intValue())));
    }

    private Event event(String title,EventCategory category,int score){return events.create(request(title,category,score));}
    private EventRequest request(String title,EventCategory category,int score){EventRequest r=new EventRequest();r.title=title;r.summary=title+"摘要";r.whatHappened="已确认发生";r.whyImportant="对开发者有价值";r.availability="AVAILABLE";r.developerImpact="可以立即评估";r.category=category;r.totalScore=score;r.impactScore=score;r.technicalScore=score;r.availabilityScore=score;r.developerValueScore=score;r.credibilityScore=score;r.freshnessScore=score;r.eventTime=LocalDateTime.now();return r;}
}
