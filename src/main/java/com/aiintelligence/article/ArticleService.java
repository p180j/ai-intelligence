package com.aiintelligence.article;

import com.aiintelligence.common.NotFoundException;
import com.aiintelligence.source.SourceService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class ArticleService {
    private final ArticleRepository repository;
    private final SourceService sourceService;

    public ArticleService(ArticleRepository repository, SourceService sourceService) {
        this.repository = repository; this.sourceService = sourceService;
    }

    public List<Article> search(Long sourceId, ArticleStatus status, LocalDateTime publishedAt, String keyword) {
        return repository.search(sourceId, status, publishedAt, keyword);
    }

    public Article findById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Article not found: " + id));
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(long id, ArticleStatus status) { repository.updateStatus(id, status); }
    public Article findByUrl(String url) { return repository.findByUrl(url).orElse(null); }
    public Article findByContentHash(String hash) { return repository.findByContentHash(hash).orElse(null); }

    @Transactional
    public Article create(ArticleRequest request) {
        sourceService.findById(request.sourceId);
        return findById(repository.insert(request));
    }
}
