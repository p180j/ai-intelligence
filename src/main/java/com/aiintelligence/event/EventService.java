package com.aiintelligence.event;

import com.aiintelligence.article.ArticleService;
import com.aiintelligence.common.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {
    private final EventRepository repository; private final ArticleService articleService;
    public EventService(EventRepository repository, ArticleService articleService){this.repository=repository;this.articleService=articleService;}
    public List<Event> findAll(){List<Event> list=repository.findAll();list.forEach(e->{e.articles=repository.findArticles(e.id);e.products=repository.findProducts(e.id);});return list;}
    public List<Event> findRecent(java.time.LocalDateTime since){return repository.findRecent(since);}
    public Event findById(long id){Event e=repository.findById(id).orElseThrow(()->new NotFoundException("Event not found: "+id));e.articles=repository.findArticles(id);e.products=repository.findProducts(id);return e;}
    public Event findByArticleId(long articleId){return repository.findByArticleId(articleId).map(e->findById(e.id)).orElse(null);}
    @Transactional public Event create(EventRequest r){return findById(repository.insert(r));}
    @Transactional public Event update(long id,EventRequest r){if(repository.update(id,r)==0)throw new NotFoundException("Event not found: "+id);return findById(id);}
    @Transactional public Event attachArticle(long eventId,long articleId,ArticleRelationRequest r){findById(eventId);articleService.findById(articleId);if(r==null)r=new ArticleRelationRequest();if(Boolean.TRUE.equals(r.primarySource))r.relationType=ArticleRelationType.PRIMARY;repository.attachArticle(eventId,articleId,r);return findById(eventId);}
    @Transactional public void detachArticle(long eventId,long articleId){findById(eventId);if(repository.detachArticle(eventId,articleId)==0)throw new NotFoundException("Article relation not found");}
    @Transactional public Event review(long id,ReviewStatus status){Event event=findById(id);if(status==null||status==ReviewStatus.PENDING)throw new IllegalArgumentException("不支持的审核状态");if(event.reviewStatus==status)return event;if(event.publishStatus!=PublishStatus.DRAFT)throw new IllegalArgumentException("已发布或已下线的事件不能修改审核状态");repository.updateReviewStatus(id,status);return findById(id);}
    @Transactional public Event publish(long id){Event event=findById(id);if(event.reviewStatus!=ReviewStatus.APPROVED)throw new IllegalArgumentException("只有已审核的事件可以发布");if(event.publishStatus==PublishStatus.PUBLISHED)return event;repository.publish(id);return findById(id);}
    @Transactional public Event offline(long id){Event event=findById(id);if(event.publishStatus==PublishStatus.OFFLINE)return event;if(event.reviewStatus!=ReviewStatus.APPROVED||event.publishStatus!=PublishStatus.PUBLISHED)throw new IllegalArgumentException("只有已发布的事件可以下线");repository.offline(id);return findById(id);}
    public List<Event> searchPublished(String keyword){List<Event> list=repository.searchPublished(keyword);list.forEach(e->{e.articles=repository.findArticles(e.id);e.products=repository.findProducts(e.id);});return list;}
}
