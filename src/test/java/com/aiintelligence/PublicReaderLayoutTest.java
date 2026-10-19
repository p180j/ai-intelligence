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
        assertTrue(css.contains(".article-shell{width:min(820px"));
        assertTrue(css.contains(".breadcrumb{display:inline-flex;width:max-content"));
        assertFalse(script.contains("暂未关联产品"));
        assertTrue(script.contains("lower.startsWith('available'))return '已正式可用'"));
        assertTrue(css.contains("grid-template-columns:repeat(auto-fit,minmax(220px,360px))"));
        assertTrue(script.contains("reader?.readerTitle||event.title"));
        assertTrue(script.contains("<h1>最新 AI 情报</h1>"));
        assertTrue(script.contains("共 ${report.events.length} 篇已发布情报"));
        assertTrue(script.contains("发布于 ${fmt(event.publishedAt||event.eventTime)}"));
    }

    @Test
    void readerArticleUsesEditorialSectionsInsteadOfAFlatParagraphDump() throws Exception {
        String css = resource("/static/public.css");
        String script = resource("/static/public.js");
        String prompt = resource("/prompts/reader-article.txt");

        assertTrue(prompt.contains("## 小标题"));
        assertTrue(prompt.contains("reader-v3"));
        assertTrue(script.contains("line.startsWith('## ')"));
        assertTrue(script.contains("<h2>${esc(section.title)}</h2>"));
        assertTrue(css.contains(".reader-section h2{"));
    }

    @Test
    void readerSectionsHaveScannableRhythmAndRenderInlineCode() throws Exception {
        String css = resource("/static/public.css");
        String script = resource("/static/public.js");
        String prompt = resource("/prompts/reader-article.txt");

        assertTrue(prompt.contains("12 to 18 Chinese characters"));
        assertTrue(script.contains("<section class=\"reader-section\">"));
        assertTrue(script.contains("<div class=\"reader-section-number\">"));
        assertTrue(script.contains("'<code>$1</code>'"));
        assertTrue(css.contains(".reader-section{"));
        assertTrue(css.contains(".reader-section-number{"));
        assertFalse(css.contains(".reader-content h2:before"));
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
