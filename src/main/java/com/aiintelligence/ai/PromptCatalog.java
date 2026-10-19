package com.aiintelligence.ai;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class PromptCatalog {
    public String articleAnalysis(){return load("article-analysis.txt");}public String eventMatching(){return load("event-matching.txt");}public String eventEnrichment(){return load("event-enrichment.txt");}public String readerArticle(){return load("reader-article.txt");}
    private String load(String name){try(InputStream in=getClass().getResourceAsStream("/prompts/"+name)){if(in==null)throw new IllegalStateException("Missing prompt: "+name);return StreamUtils.copyToString(in,StandardCharsets.UTF_8);}catch(IOException e){throw new IllegalStateException(e);}}
}
