package com.aiintelligence.collector;

import com.aiintelligence.source.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

@Component
public class RssArticleCollector implements ArticleCollector {
    private final ObjectMapper mapper;
    public RssArticleCollector(ObjectMapper mapper){this.mapper=mapper;}
    public boolean supports(Source s){return "RSS".equalsIgnoreCase(s.collectorType)||("RSS".equals(s.sourceType.name())&&s.feedUrl!=null);}
    public CollectResult collect(Source s){try(InputStream in=new URL(s.feedUrl).openStream()){DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);f.setFeature("http://xml.org/sax/features/external-general-entities",false);f.setFeature("http://xml.org/sax/features/external-parameter-entities",false);f.setNamespaceAware(true);Document d=f.newDocumentBuilder().parse(in);NodeList nodes=d.getElementsByTagName("item");if(nodes.getLength()==0)nodes=d.getElementsByTagNameNS("*","entry");List<ArticleCandidate> list=new ArrayList<>();int maxItems=maxItems(s);for(int i=0;i<nodes.getLength()&&list.size()<maxItems;i++){Element e=(Element)nodes.item(i);ArticleCandidate c=new ArticleCandidate();c.sourceId=s.id;c.title=text(e,"title");c.url=link(e);c.author=first(text(e,"author"),text(e,"creator"));c.rawContent=first(text(e,"content"),text(e,"encoded"),text(e,"description"),text(e,"summary"));c.publishedAt=date(first(text(e,"pubDate"),text(e,"published"),text(e,"updated")));if(c.title!=null&&c.url!=null)list.add(c);}return CollectResult.of(list);}catch(Exception e){throw new IllegalStateException("RSS collect failed: "+e.getMessage(),e);}}
    private int maxItems(Source source){if(source.collectConfig==null||source.collectConfig.trim().isEmpty())return 20;try{return Math.max(1,Math.min(100,mapper.readTree(source.collectConfig).path("maxItems").asInt(20)));}catch(Exception e){throw new IllegalArgumentException("Invalid RSS collectConfig",e);}}
    private String text(Element e,String name){NodeList n=e.getElementsByTagNameNS("*",name);if(n.getLength()==0)n=e.getElementsByTagName(name);return n.getLength()==0?null:n.item(0).getTextContent().trim();}
    private String link(Element e){NodeList n=e.getElementsByTagNameNS("*","link");if(n.getLength()==0)n=e.getElementsByTagName("link");if(n.getLength()==0)return null;Element link=(Element)n.item(0);return link.hasAttribute("href")?link.getAttribute("href"):link.getTextContent().trim();}
    private String first(String...v){for(String s:v)if(s!=null&&!s.isEmpty())return s;return null;}
    private LocalDateTime date(String v){if(v==null)return null;try{return ZonedDateTime.parse(v,DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();}catch(Exception ignored){}try{return OffsetDateTime.parse(v).toLocalDateTime();}catch(Exception ignored){return null;}}
}
