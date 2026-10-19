package com.aiintelligence.article;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/articles")
public class ArticleController {
    private final ArticleService service;
    public ArticleController(ArticleService service) { this.service = service; }

    @GetMapping
    public List<Article> list(@RequestParam(required=false) Long sourceId,
                              @RequestParam(required=false) ArticleStatus status,
                              @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishedAt,
                              @RequestParam(required=false) String keyword) {
        return service.search(sourceId, status, publishedAt, keyword);
    }
    @GetMapping("/{id}") public Article get(@PathVariable long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Article create(@Valid @RequestBody ArticleRequest request) { return service.create(request); }
}

