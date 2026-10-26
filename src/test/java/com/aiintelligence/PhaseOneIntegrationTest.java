package com.aiintelligence;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest @AutoConfigureMockMvc(addFilters=false) @DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PhaseOneIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper mapper; @Autowired com.aiintelligence.event.EventService events; @Autowired com.aiintelligence.source.SourceService sources;
    @Autowired com.aiintelligence.event.PipelineRepository pipeline; @Autowired JdbcTemplate jdbc;

    @Test void completeAcceptanceFlow() throws Exception {
        long source=id(postJson("/admin/sources","{\"name\":\"Anthropic\",\"sourceType\":\"MANUAL\",\"priority\":10}"));
        long article=id(postJson("/admin/articles","{\"sourceId\":"+source+",\"title\":\"Claude Code capability\",\"url\":\"https://anthropic.example/claude-code\",\"publishedAt\":\"2026-09-21T10:00:00\"}"));
        long event=id(postJson("/admin/events","{\"title\":\"Claude Code launches XXX\",\"category\":\"MUST_READ\",\"eventTime\":\"2026-09-21T10:00:00\"}"));
        mvc.perform(post("/admin/events/{eventId}/articles/{articleId}",event,article).contentType(MediaType.APPLICATION_JSON).content("{\"relationType\":\"PRIMARY\",\"primarySource\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.articles[0].primarySource").value(true));
        long product=id(postJson("/admin/products","{\"name\":\"Claude Code\",\"slug\":\"claude-code\",\"companyName\":\"Anthropic\"}"));
        mvc.perform(post("/admin/products/{productId}/events/{eventId}",product,event).contentType(MediaType.APPLICATION_JSON).content("{\"relationType\":\"RELEASE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.events[0].eventId").value(event));
        mvc.perform(get("/admin/events/{id}",event)).andExpect(status().isOk()).andExpect(jsonPath("$.articles[0].sourceName").value("Anthropic")).andExpect(jsonPath("$.products[0].slug").value("claude-code"));
        mvc.perform(get("/admin/products/{id}",product)).andExpect(status().isOk()).andExpect(jsonPath("$.events[0].title").value("Claude Code launches XXX"));
        mvc.perform(delete("/admin/events/{eventId}/articles/{articleId}",event,article)).andExpect(status().isNoContent());
        mvc.perform(get("/admin/events/{id}",event)).andExpect(jsonPath("$.articles",hasSize(0)));
    }

    @Test void sourceCanBeUpdatedDisabledAndQueried() throws Exception {
        long source=id(postJson("/admin/sources","{\"name\":\"OpenAI\",\"sourceType\":\"RSS\"}"));
        mvc.perform(put("/admin/sources/{id}",source).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"OpenAI News\",\"sourceType\":\"RSS\",\"priority\":5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("OpenAI News"));
        mvc.perform(delete("/admin/sources/{id}",source)).andExpect(status().isNoContent());
        mvc.perform(get("/admin/sources/{id}",source)).andExpect(jsonPath("$.enabled").value(false));
    }

    @Test void sourceClosesAfterSixConsecutiveFailuresAndCanBeEnabled() throws Exception {
        long source=id(postJson("/admin/sources","{\"name\":\"Unstable RSS\",\"sourceType\":\"RSS\",\"feedUrl\":\"https://example.test/feed.xml\"}"));
        for(int i=0;i<5;i++)sources.collectFailed(source,"timeout");
        mvc.perform(get("/admin/sources/{id}",source)).andExpect(jsonPath("$.enabled").value(true)).andExpect(jsonPath("$.failureCount").value(5));
        sources.collectFailed(source,"timeout");
        mvc.perform(get("/admin/sources/{id}",source)).andExpect(jsonPath("$.enabled").value(false)).andExpect(jsonPath("$.failureCount").value(6));
        mvc.perform(post("/admin/sources/{id}/enable",source)).andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(true)).andExpect(jsonPath("$.failureCount").value(0));
    }

    @Test void onlyUnusedSourceCanBeDeletedPermanently() throws Exception {
        long unused=id(postJson("/admin/sources","{\"name\":\"Unused\",\"sourceType\":\"RSS\"}"));
        mvc.perform(delete("/admin/sources/{id}/permanent",unused)).andExpect(status().isNoContent());
        mvc.perform(get("/admin/sources/{id}",unused)).andExpect(status().isNotFound());
        long used=id(postJson("/admin/sources","{\"name\":\"Used\",\"sourceType\":\"MANUAL\"}"));
        postJson("/admin/articles","{\"sourceId\":"+used+",\"title\":\"历史文章\",\"url\":\"https://example.test/history\"}");
        mvc.perform(delete("/admin/sources/{id}/permanent",used)).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("该信源已有历史文章，不能删除，请改为关闭"));
    }

    @Test void duplicateArticleUrlAndProductSlugAreRejected() throws Exception {
        long source=id(postJson("/admin/sources","{\"name\":\"GitHub\",\"sourceType\":\"GITHUB\"}"));
        String article="{\"sourceId\":"+source+",\"title\":\"Release\",\"url\":\"https://example.test/release\"}";
        postJson("/admin/articles",article);
        mvc.perform(post("/admin/articles").contentType(MediaType.APPLICATION_JSON).content(article)).andExpect(status().isConflict());
        String product="{\"name\":\"Codex\",\"slug\":\"codex\"}";
        postJson("/admin/products",product);
        mvc.perform(post("/admin/products").contentType(MediaType.APPLICATION_JSON).content(product)).andExpect(status().isConflict());
    }

    @Test void articleFiltersAndEventEditingWork() throws Exception {
        long source=id(postJson("/admin/sources","{\"name\":\"arXiv\",\"sourceType\":\"API\"}"));
        postJson("/admin/articles","{\"sourceId\":"+source+",\"title\":\"Agent paper\",\"url\":\"https://arxiv.example/1\",\"collectStatus\":\"PROCESSED\",\"publishedAt\":\"2026-09-20T00:00:00\"}");
        mvc.perform(get("/admin/articles").param("sourceId",String.valueOf(source)).param("status","PROCESSED").param("keyword","Agent"))
                .andExpect(status().isOk()).andExpect(jsonPath("$",hasSize(1)));
        long event=id(postJson("/admin/events","{\"title\":\"Initial title\"}"));
        mvc.perform(put("/admin/events/{id}",event).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Edited title\",\"reviewStatus\":\"APPROVED\",\"publishStatus\":\"PUBLISHED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Edited title")).andExpect(jsonPath("$.reviewStatus").value("PENDING")).andExpect(jsonPath("$.publishStatus").value("DRAFT"));
        events.review(event,com.aiintelligence.event.ReviewStatus.APPROVED);
        events.publish(event);
        mvc.perform(get("/admin/events/{id}",event)).andExpect(status().isOk()).andExpect(jsonPath("$.reviewStatus").value("APPROVED")).andExpect(jsonPath("$.publishStatus").value("PUBLISHED"));
    }

    @Test void knownProductVariantsAreLinkedAutomaticallyWithoutCreatingCandidateTasks() throws Exception {
        long product=id(postJson("/admin/products","{\"name\":\"Auto Link Code\",\"slug\":\"auto-link-code\",\"companyName\":\"Test Lab\"}"));
        long versionEvent=id(postJson("/admin/events","{\"title\":\"Auto Link Code 2.0 发布\"}"));
        long unrelatedEvent=id(postJson("/admin/events","{\"title\":\"未知模型发布\"}"));
        assertTrue(pipeline.linkProductIfKnown(versionEvent,"Auto Link Code 2.0"));
        assertTrue(!pipeline.linkProductIfKnown(unrelatedEvent,"Auto Link"));
        mvc.perform(get("/admin/events/{id}",versionEvent)).andExpect(jsonPath("$.products[0].productId").value(product));
        mvc.perform(get("/admin/events/{id}",unrelatedEvent)).andExpect(jsonPath("$.products",hasSize(0)));
        org.junit.jupiter.api.Assertions.assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM ai_product_candidate WHERE event_id IN (?,?)",Integer.class,versionEvent,unrelatedEvent));
    }

    @Test void productLibraryReconcilesExistingCandidatesWithoutManualActions() throws Exception {
        long event=id(postJson("/admin/events","{\"title\":\"GitHub Copilot IDE 更新\"}"));
        long product=id(postJson("/admin/products","{\"name\":\"GitHub Copilot\",\"slug\":\"github-copilot\",\"companyName\":\"GitHub\"}"));
        jdbc.update("INSERT INTO ai_product_candidate(event_id,name,entity_type) VALUES(?,?,?)",event,"GitHub Copilot for JetBrains","PRODUCT");
        mvc.perform(post("/admin/products/reconcile")).andExpect(status().isOk()).andExpect(content().string("1"));
        mvc.perform(get("/admin/events/{id}",event)).andExpect(jsonPath("$.products[0].productId").value(product));
        org.junit.jupiter.api.Assertions.assertEquals("CONFIRMED",jdbc.queryForObject("SELECT status FROM ai_product_candidate WHERE event_id=?",String.class,event));
    }

    private String postJson(String url,String body)throws Exception{return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();}
    private long id(String json)throws Exception{JsonNode node=mapper.readTree(json);return node.get("id").asLong();}
}
