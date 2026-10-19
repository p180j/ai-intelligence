package com.aiintelligence;

import static org.hamcrest.Matchers.*;
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

@SpringBootTest @AutoConfigureMockMvc(addFilters=false) @DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PhaseOneIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper mapper; @Autowired com.aiintelligence.event.EventService events;

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

    private String postJson(String url,String body)throws Exception{return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();}
    private long id(String json)throws Exception{JsonNode node=mapper.readTree(json);return node.get("id").asLong();}
}
