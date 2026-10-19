package com.aiintelligence.collector;

import com.aiintelligence.source.Source;
import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class GitHubReleaseCollector implements ArticleCollector {
    private final ObjectMapper mapper; public GitHubReleaseCollector(ObjectMapper mapper){this.mapper=mapper;}
    public boolean supports(Source s){return "GITHUB".equalsIgnoreCase(s.collectorType)||"GITHUB".equals(s.sourceType.name());}
    public CollectResult collect(Source s){try{JsonNode cfg=mapper.readTree(s.collectConfig);String owner=cfg.path("owner").asText();String repo=cfg.path("repo").asText();if(owner.isEmpty()||repo.isEmpty())throw new IllegalArgumentException("GitHub source requires owner/repo");HttpURLConnection c=(HttpURLConnection)new URL("https://api.github.com/repos/"+owner+"/"+repo+"/releases?per_page=20").openConnection();c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","ai-intelligence/1.0");JsonNode root=mapper.readTree(c.getInputStream());List<ArticleCandidate> out=new ArrayList<>();for(JsonNode n:root){ArticleCandidate a=new ArticleCandidate();a.sourceId=s.id;a.title=n.path("name").asText(n.path("tag_name").asText());a.url=n.path("html_url").asText();a.author=n.path("author").path("login").asText(null);a.rawContent=n.path("body").asText();String p=n.path("published_at").asText(null);a.publishedAt=p==null?null:OffsetDateTime.parse(p).toLocalDateTime();out.add(a);}return CollectResult.of(out);}catch(Exception e){throw new IllegalStateException("GitHub release collect failed: "+e.getMessage(),e);}}
}
