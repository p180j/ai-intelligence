package com.aiintelligence.source;

import com.aiintelligence.common.ConflictException;
import com.aiintelligence.common.NotFoundException;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SourceService {
    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private final SourceRepository repository;

    public SourceService(SourceRepository repository) {
        this.repository = repository;
    }

    @PostConstruct public void closeUnhealthySources() { repository.disableExceededFailures(MAX_CONSECUTIVE_FAILURES); }

    public List<Source> findAll() { return repository.findAll(); }
    public List<Source> findEnabled() { return repository.findEnabled(); }
    public void collectSucceeded(long id, int count) { repository.collectSucceeded(id, count); }
    public void testSucceeded(long id, int count) { repository.collectSucceeded(id, count); }
    @Transactional public void collectFailed(long id, String message) { repository.collectFailed(id, message == null ? "Unknown collector error" : message.substring(0, Math.min(1000, message.length())));repository.disableWhenFailureCountExceeds(id,MAX_CONSECUTIVE_FAILURES); }

    public Source findById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Source not found: " + id));
    }

    @Transactional
    public Source create(SourceRequest request) { return findById(repository.insert(request)); }

    @Transactional
    public Source update(long id, SourceRequest request) {
        if (repository.update(id, request) == 0) throw new NotFoundException("Source not found: " + id);
        return findById(id);
    }

    @Transactional
    public void disable(long id) {
        if (repository.disable(id) == 0) throw new NotFoundException("Source not found: " + id);
    }

    @Transactional
    public Source enable(long id) {
        if (repository.enable(id) == 0) throw new NotFoundException("Source not found: " + id);
        return findById(id);
    }

    @Transactional
    public void delete(long id) {
        findById(id);
        if (repository.countArticles(id) > 0) throw new ConflictException("该信源已有历史文章，不能删除，请改为关闭");
        repository.delete(id);
    }
}
