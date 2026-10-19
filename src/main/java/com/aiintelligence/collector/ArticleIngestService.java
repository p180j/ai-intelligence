package com.aiintelligence.collector;

import com.aiintelligence.article.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;

@Service
public class ArticleIngestService {
    private final ArticleService articles; private final UrlNormalizer urls; private final ArticleContentCleaner cleaner;
    public ArticleIngestService(ArticleService articles,UrlNormalizer urls,ArticleContentCleaner cleaner){this.articles=articles;this.urls=urls;this.cleaner=cleaner;}
    public Article ingest(ArticleCandidate c){String url=urls.normalize(c.url);Article existing=articles.findByUrl(url);if(existing!=null)return existing;String clean=cleaner.clean(c.rawContent);String hash=hash(clean);if(hash!=null){existing=articles.findByContentHash(hash);if(existing!=null)return existing;}ArticleRequest r=new ArticleRequest();r.sourceId=c.sourceId;r.title=c.title;r.url=url;r.author=c.author;r.language=c.language;r.publishedAt=c.publishedAt;r.rawContent=c.rawContent;r.cleanContent=clean;r.contentHash=hash;r.collectStatus=ArticleStatus.NEW;r.collectedAt=java.time.LocalDateTime.now();return articles.create(r);}
    private String hash(String text){if(text==null||text.isEmpty())return null;try{byte[] b=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format("%02x",v));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}
