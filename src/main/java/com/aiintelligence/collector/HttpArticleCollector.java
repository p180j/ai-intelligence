package com.aiintelligence.collector;

import com.aiintelligence.source.Source;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class HttpArticleCollector implements ArticleCollector {
    public boolean supports(Source s){return "HTTP".equalsIgnoreCase(s.collectorType)||"HTML".equals(s.sourceType.name());}
    public CollectResult collect(Source s){String url=s.feedUrl!=null?s.feedUrl:s.baseUrl;if(url==null)throw new IllegalArgumentException("HTTP source requires feedUrl or baseUrl");try{Document d=Jsoup.connect(url).userAgent("ai-intelligence/1.0").timeout(15000).get();ArticleCandidate c=new ArticleCandidate();c.sourceId=s.id;c.title=d.title();c.url=d.selectFirst("link[rel=canonical]")!=null?d.selectFirst("link[rel=canonical]").absUrl("href"):url;c.author=meta(d,"author");c.rawContent=d.selectFirst("article")!=null?d.selectFirst("article").html():d.body().html();String p=meta(d,"article:published_time");try{c.publishedAt=p==null?null:OffsetDateTime.parse(p).toLocalDateTime();}catch(Exception ignored){}return CollectResult.of(Collections.singletonList(c));}catch(Exception e){throw new IllegalStateException("HTTP collect failed: "+e.getMessage(),e);}}
    private String meta(Document d,String name){org.jsoup.nodes.Element e=d.selectFirst("meta[name="+name+"],meta[property="+name+"]");return e==null?null:e.attr("content");}
}
