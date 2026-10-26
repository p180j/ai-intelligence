package com.aiintelligence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class PublicReaderLayoutTest {
    @Test
    void articleUsesReadableWidthWithoutDecorativeDropCap() throws Exception {
        String css = resource("/static/public.css");
        String script = resource("/static/public.js");

        assertFalse(css.contains(".article-opening p:first-letter"));
        assertTrue(css.contains(".article-shell{width:min(1200px"));
        assertTrue(css.contains("grid-template-columns:180px minmax(0,760px) 180px"));
        assertTrue(css.contains(".article-return{width:min(760px,100%)"));
        assertTrue(css.contains(".article-header{width:min(760px,100%)"));
        assertTrue(script.contains("<div class=\"article-return\"><a class=\"back-link\" href=\"/\">← 返回今日日报</a></div>"));
        assertTrue(script.contains("<span class=\"article-category\">${sectionLabels[event.category]||'AI 情报'}</span>"));
        assertTrue(css.contains("--public-font:Inter,-apple-system"));
        assertFalse(css.contains("font-family:Georgia"));
        assertFalse(script.contains("暂未关联产品"));
        assertTrue(script.contains("lower.startsWith('available'))return '已正式可用'"));
        assertTrue(css.contains("grid-template-columns:repeat(auto-fit,minmax(220px,360px))"));
        assertTrue(script.contains("reader?.readerTitle||event.title"));
        assertTrue(script.contains("<h1>最新 AI 情报</h1>"));
        assertTrue(script.contains("共 ${report.events.length} 篇已发布情报"));
        assertTrue(script.contains("发布于 ${fmt(event.publishedAt||event.eventTime)}"));
        assertTrue(script.contains("NORMAL:'其他 AI 动态'"));
    }

    @Test
    void readerArticleUsesHeadingsOnlyWhenTheArticleIsLongEnough() throws Exception {
        String css = resource("/static/public.css");
        String script = resource("/static/public.js");
        String prompt = resource("/prompts/reader-article.txt");

        assertTrue(prompt.contains("普通短稿不要使用小标题"));
        assertTrue(prompt.contains("reader-v5"));
        assertTrue(script.contains("line.startsWith('## ')"));
        assertTrue(script.contains("contentLength>=800"));
        assertTrue(script.contains("reader-content${readerUsesHeadings?'':' is-continuous'}"));
        assertTrue(script.contains("showHeadings&&section.title?`<h2>${esc(section.title)}</h2>`:''"));
        assertTrue(css.contains(".reader-section h2{"));
        assertTrue(css.contains(".reader-content.is-continuous .reader-section{"));
    }

    @Test
    void readerSectionsUseNaturalEditorialFlowAndRenderRichBlocks() throws Exception {
        String css = resource("/static/public.css");
        String script = resource("/static/public.js");
        String prompt = resource("/prompts/reader-article.txt");

        assertTrue(prompt.contains("10 to 16 Chinese characters"));
        assertTrue(script.contains("<section class=\"reader-section${showHeadings&&section.title?' has-heading':''}\" id=\"reader-section-${index+1}\">"));
        assertFalse(script.contains("reader-section-number"));
        assertTrue(script.contains("<code>${esc(token.substring(1,token.length-1))}</code>"));
        assertTrue(script.contains("<aside class=\"article-rail\">"));
        assertTrue(script.contains("links.length>=5&&contentLength>=1200"));
        assertFalse(script.contains("reader-term"));
        assertTrue(script.contains("<strong class=\"reader-emphasis\">"));
        assertTrue(css.contains(".reader-section{"));
        assertTrue(css.contains(".reader-list{"));
        assertTrue(css.contains(".reader-section blockquote{"));
        assertTrue(css.contains(".reader-emphasis{color:var(--public-accent);font-weight:700}"));
        assertFalse(css.contains(".reader-content h2:before"));
    }

    @Test
    void semanticEmphasisComesFromTheApprovedReaderCopyInsteadOfTokenGuessing() throws Exception {
        String script = resource("/static/public.js");
        String prompt = resource("/prompts/reader-article.txt");

        assertFalse(script.contains("[a-z][A-Z]"));
        assertFalse(script.contains("reader-term"));
        assertTrue(prompt.contains("`**重点内容**`"));
        assertTrue(prompt.contains("每段最多一到两处"));
        assertTrue(prompt.contains("不得仅因为是产品名、模型名或版本号就强调"));
    }

    @Test
    void publicPagesShareFlatMediaNavigation() throws Exception {
        String css = resource("/static/public.css");
        String event = resource("/static/event.html");
        String script = resource("/static/public.js");

        for (String page : new String[] {"index.html", "event.html", "product.html", "search.html"}) {
            String html = resource("/static/" + page);
            assertTrue(html.contains("<span>AI</span><strong>前沿情报</strong>"));
            assertTrue(html.contains("<nav class=\"site-menu\" aria-label=\"主导航\">"));
            assertTrue(html.contains("href=\"/#must-read\">必看</a>"));
        }
        assertTrue(css.contains(".site-nav{width:min(1080px,100%);height:64px"));
        assertTrue(css.contains("position:sticky"));
        assertTrue(css.contains("border-bottom:1px solid var(--public-line)"));
        assertFalse(css.contains("border-radius:16px"));
        assertTrue(event.contains("<a class=\"site-reader-back\" href=\"/\">← 返回日报</a>"));
        assertTrue(css.contains(".article-page.is-reading .site-reader-back{display:inline-flex}"));
        assertTrue(script.contains("document.body.classList.toggle('is-reading'"));
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
