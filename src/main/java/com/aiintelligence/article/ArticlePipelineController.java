package com.aiintelligence.article;

import com.aiintelligence.collector.CollectorService;
import com.aiintelligence.event.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/articles")
public class ArticlePipelineController {
    private final ArticleProcessingService processing;private final CollectorService collector;private final ArticleService articles;
    public ArticlePipelineController(ArticleProcessingService processing,CollectorService collector,ArticleService articles){this.processing=processing;this.collector=collector;this.articles=articles;}
    @PostMapping("/{id}/process") public Event process(@PathVariable long id){return processing.process(id,false);}
    @PostMapping("/{id}/reprocess") public Event reprocess(@PathVariable long id){return processing.reprocess(id);}
    @PostMapping("/{id}/ignore") public void ignore(@PathVariable long id){articles.findById(id);articles.updateStatus(id,ArticleStatus.IGNORED);}
    @PostMapping("/import-url") public Article importUrl(@RequestBody ImportUrlRequest r){return collector.importUrl(r.sourceId,r.url);}
    public static class ImportUrlRequest {@NotNull public Long sourceId;@NotBlank public String url;}
}
