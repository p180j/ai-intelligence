package com.aiintelligence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AdminContentManagementTest {
    @Test
    void adminExposesArticleRelationActions() throws Exception {
        String script = resource("/static/admin/admin.js");

        assertTrue(script.contains("/articles/${articleId}"));
        assertTrue(script.contains("method:'DELETE'"));
        assertTrue(script.contains("已关联 ${e.articles.length} 篇文章"));
        assertTrue(script.contains("apiOptional('/admin/quality/events/'"));
        assertTrue(script.contains("/${id}/republish"));
        assertTrue(script.contains("const text=await r.text()"));
        assertTrue(script.contains("采集失败："));
        assertTrue(script.contains("approveEvent("));
        assertTrue(script.contains("/events/${id}/publish"));
        assertTrue(script.contains("events('approved')"));
        assertTrue(script.contains("events('published')"));
        assertTrue(script.contains("/${id}/reader/regenerate"));
        assertTrue(script.contains("重写用户文章"));
        assertTrue(script.contains("重新生成事件事实"));
        assertTrue(script.contains("查看用户页面"));
    }

    @Test
    void eventReviewDetailShowsCurrentWorkflowState() throws Exception {
        String script = resource("/static/admin/admin.js");

        assertTrue(script.contains("event-status-summary"));
        assertTrue(script.contains("审核状态"));
        assertTrue(script.contains("发布状态"));
        assertTrue(script.contains("下一步"));
    }

    private String resource(String path) throws Exception {
        try (java.io.InputStream input = getClass().getResourceAsStream(path)) {
            if (input == null) throw new IllegalStateException("Missing resource: " + path);
            byte[] bytes = new byte[input.available()];
            int read = input.read(bytes);
            if (read != bytes.length) throw new IllegalStateException("Could not read resource: " + path);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
