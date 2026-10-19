package com.aiintelligence.quality;

import com.aiintelligence.article.ArticleService;
import com.aiintelligence.event.EventService;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QualityService {
    private final QualityRepository repository;private final EventService events;private final ArticleService articles;
    public QualityService(QualityRepository repository,EventService events,ArticleService articles){this.repository=repository;this.events=events;this.articles=articles;}
    @Transactional public EventFeedback save(long eventId,EventFeedbackRequest r){events.findById(eventId);if(r.reviewSeconds!=null&&(r.reviewSeconds<0||r.reviewSeconds>86400))throw new IllegalArgumentException("Invalid review duration");if(Boolean.TRUE.equals(r.wrongMerge)){articles.findById(required(r.articleAId,"articleAId"));articles.findById(required(r.articleBId,"articleBId"));repository.saveEval(r.articleAId,r.articleBId,MatchExpectation.DIFFERENT_EVENT,"WRONG_MERGE",eventId,r.notes);}if(r.duplicateEventId!=null){events.findById(r.duplicateEventId);repository.saveEval(repository.primaryArticle(eventId),repository.primaryArticle(r.duplicateEventId),MatchExpectation.SAME_EVENT,"MISSED_MERGE",eventId,r.notes);}repository.saveFeedback(eventId,r);return repository.findFeedback(eventId).get();}
    public EventFeedback find(long eventId){events.findById(eventId);return repository.findFeedback(eventId).orElse(null);}
    @Transactional public EvalCase addEval(EvalCaseRequest r){articles.findById(r.articleAId);articles.findById(r.articleBId);repository.saveEval(r.articleAId,r.articleBId,r.expectedResult,"MANUAL",null,r.notes);return repository.findEvalCases().stream().filter(x->same(x,r.articleAId,r.articleBId)).findFirst().get();}
    public List<EvalCase> evalCases(){return repository.findEvalCases();}
    public Map<String,Object> stats(LocalDate date){return repository.dailyStats(date);}
    private long required(Long value,String field){if(value==null)throw new IllegalArgumentException(field+" is required");return value;}
    private boolean same(EvalCase c,long a,long b){return(c.articleAId==Math.min(a,b)&&c.articleBId==Math.max(a,b));}
}
