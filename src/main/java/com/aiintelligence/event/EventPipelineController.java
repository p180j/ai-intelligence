package com.aiintelligence.event;

import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController @RequestMapping("/admin/events")
public class EventPipelineController {
    private final ArticleProcessingService processing;private final EventService events;private final com.aiintelligence.reader.ReaderArticleService readers;
    public EventPipelineController(ArticleProcessingService processing,EventService events,com.aiintelligence.reader.ReaderArticleService readers){this.processing=processing;this.events=events;this.readers=readers;}
    @PostMapping("/{id}/regenerate") public Event regenerate(@PathVariable long id){return processing.refresh(id);}
    @PostMapping("/{id}/reader/regenerate") public com.aiintelligence.reader.ReaderArticle regenerateReader(@PathVariable long id){return readers.generate(id);}
    @Transactional
    @PostMapping("/{id}/approve") public Event approve(@PathVariable long id){events.review(id,ReviewStatus.APPROVED);readers.generate(id);return events.findById(id);}
    @PostMapping("/{id}/reject") public Event reject(@PathVariable long id){return events.review(id,ReviewStatus.REJECTED);}
    @PostMapping("/{id}/publish") public Event publish(@PathVariable long id){return events.publish(id);}
    @PostMapping("/{id}/offline") public Event offline(@PathVariable long id){return events.offline(id);}
    @PostMapping("/{id}/republish") public Event republish(@PathVariable long id){return events.publish(id);}
}
