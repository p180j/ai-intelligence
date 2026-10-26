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
        assertTrue(script.contains("const text=await response.text()"));
        assertTrue(script.contains("采集失败："));
        assertTrue(script.contains("连续失败超过 5 次会自动关闭"));
        assertTrue(script.contains("新增信源"));
        assertTrue(script.contains("全部采集"));
        assertTrue(script.contains("/admin/collect/run"));
        assertTrue(!script.contains(">立即采集</button>"));
        assertTrue(script.contains("测试中…"));
        assertTrue(script.contains("/permanent"));
        assertTrue(script.contains("/enable"));
        assertTrue(script.contains("approveEvent("));
        assertTrue(script.contains("/events/${id}/publish"));
        assertTrue(script.contains("events('approved')"));
        assertTrue(script.contains("events('published')"));
        assertTrue(script.contains("/${id}/reader/regenerate"));
        assertTrue(script.contains("重写用户文章"));
        assertTrue(script.contains("重新生成事件事实"));
        assertTrue(script.contains("查看用户页面"));
        assertTrue(script.contains("eventActionLabel"));
        assertTrue(script.contains("查看内容"));
    }

    @Test
    void eventReviewDetailShowsCurrentWorkflowState() throws Exception {
        String script = resource("/static/admin/admin.js");

        assertTrue(script.contains("event-status-summary"));
        assertTrue(script.contains("审核状态"));
        assertTrue(script.contains("发布状态"));
        assertTrue(script.contains("下一步"));
    }

    @Test
    void sourceActionsStayOnOneCompactRow() throws Exception {
        String css = resource("/static/app.css");

        assertTrue(css.contains(".source-actions{display:flex;min-width:238px"));
        assertTrue(css.contains("flex-wrap:nowrap;white-space:nowrap"));
        assertTrue(css.contains(".source-actions .text-button,.source-actions .danger"));
    }

    @Test
    void dashboardOwnsContentAndReportWorkflowEntries() throws Exception {
        String html = resource("/static/admin/index.html");
        String script = resource("/static/admin/admin.js");

        assertTrue(!html.contains("data-tab=\"content\">内容管理"));
        assertTrue(!html.contains("data-tab=\"reports\">每日简报"));
        assertTrue(!html.contains("data-tab=\"events\">事件审核"));
        assertTrue(!html.contains("data-tab=\"articles\">文章管理"));
        assertTrue(!html.contains("data-tab=\"quality\">评测样本"));
        assertTrue(script.contains("管理事件内容"));
        assertTrue(script.contains("管理每日简报"));
        assertTrue(script.contains("contentTabs(tab)"));
        assertTrue(script.contains("事件审核"));
        assertTrue(script.contains("原始文章"));
        assertTrue(script.indexOf("管理事件内容") < script.indexOf("<h2>今天</h2>"));
    }

    @Test
    void expiredAdminSessionReturnsToLoginInsteadOfParsingLoginHtmlAsReportData() throws Exception {
        String login = resource("/static/login.html");
        String script = resource("/static/admin/admin.js");

        assertTrue(script.contains("response.redirected"));
        assertTrue(script.contains("new URL(response.url).pathname==='/login'"));
        assertTrue(script.contains("window.location.assign('/login?expired')"));
        assertTrue(script.contains("登录状态已失效，请重新登录"));
        assertTrue(login.contains("id=\"session-expired\""));
        assertTrue(script.contains("<h2>AI 产品库</h2>"));
        assertTrue(script.contains("<h2>每日简报</h2>"));
    }

    @Test
    void productLibraryUsesTheApprovedCatalogLayoutWithoutCandidateActions() throws Exception {
        String css = resource("/static/app.css");
        String script = resource("/static/admin/admin.js");

        assertTrue(script.contains("<h2>AI 产品库</h2>"));
        assertTrue(script.contains("正式产品档案"));
        assertTrue(script.contains("搜索产品或公司"));
        assertTrue(script.contains("新建产品"));
        assertTrue(script.contains("关联事件"));
        assertTrue(script.contains("查看用户页"));
        assertTrue(script.contains("/admin/products/reconcile"));
        assertTrue(!script.contains("/admin/product-candidates"));
        assertTrue(!script.contains("确认并创建"));
        assertTrue(!script.contains("ignoreCandidate"));
        assertTrue(css.contains(".product-library"));
        assertTrue(css.contains(".product-catalog"));
        assertTrue(css.contains(".status-badge"));
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
