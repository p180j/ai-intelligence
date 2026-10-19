package com.aiintelligence.collector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class ArticleContentCleaner {
    public String clean(String html) {
        if (html == null) return null;
        Document document=Jsoup.parse(html); document.select("script,style,nav,header,footer,aside,noscript").remove();
        return document.text().replaceAll("\\s+", " ").trim();
    }
}
