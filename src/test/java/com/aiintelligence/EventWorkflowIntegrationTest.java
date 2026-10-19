package com.aiintelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.aiintelligence.event.Event;
import com.aiintelligence.event.EventCategory;
import com.aiintelligence.event.EventRequest;
import com.aiintelligence.event.EventService;
import com.aiintelligence.event.PublishStatus;
import com.aiintelligence.event.ReviewStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EventWorkflowIntegrationTest {
    @Autowired EventService events;

    @Test
    void createAndContentUpdateCannotBypassWorkflowActions() {
        EventRequest injected = request("不能直接发布");
        injected.reviewStatus = ReviewStatus.APPROVED;
        injected.publishStatus = PublishStatus.PUBLISHED;
        injected.publishedAt = LocalDateTime.now().minusDays(1);

        Event created = events.create(injected);

        assertEquals(ReviewStatus.PENDING, created.reviewStatus);
        assertEquals(PublishStatus.DRAFT, created.publishStatus);
        assertNull(created.publishedAt);

        events.review(created.id, ReviewStatus.APPROVED);
        Event published = events.publish(created.id);
        LocalDateTime publishedAt = published.publishedAt;

        EventRequest edit = request("只修改内容");
        edit.reviewStatus = ReviewStatus.PENDING;
        edit.publishStatus = PublishStatus.DRAFT;
        edit.publishedAt = null;
        Event updated = events.update(created.id, edit);

        assertEquals(ReviewStatus.APPROVED, updated.reviewStatus);
        assertEquals(PublishStatus.PUBLISHED, updated.publishStatus);
        assertEquals(publishedAt, updated.publishedAt);
    }

    @Test
    void lifecycleRejectsInvalidTransitionsAndSupportsRepublish() {
        Event created = events.create(request("标准流转"));

        assertThrows(IllegalArgumentException.class, () -> events.publish(created.id));
        assertThrows(IllegalArgumentException.class, () -> events.offline(created.id));

        Event approved = events.review(created.id, ReviewStatus.APPROVED);
        assertEquals(PublishStatus.DRAFT, approved.publishStatus);

        Event published = events.publish(created.id);
        assertNotNull(published.publishedAt);
        assertThrows(IllegalArgumentException.class, () -> events.review(created.id, ReviewStatus.REJECTED));

        Event offline = events.offline(created.id);
        assertEquals(ReviewStatus.APPROVED, offline.reviewStatus);
        assertEquals(PublishStatus.OFFLINE, offline.publishStatus);

        Event republished = events.publish(created.id);
        assertEquals(ReviewStatus.APPROVED, republished.reviewStatus);
        assertEquals(PublishStatus.PUBLISHED, republished.publishStatus);
    }

    @Test
    void rejectedEventMustBeApprovedBeforeItCanBePublished() {
        Event created = events.create(request("拒绝后重审"));
        Event rejected = events.review(created.id, ReviewStatus.REJECTED);

        assertEquals(ReviewStatus.REJECTED, rejected.reviewStatus);
        assertEquals(PublishStatus.DRAFT, rejected.publishStatus);
        assertThrows(IllegalArgumentException.class, () -> events.publish(created.id));

        Event approved = events.review(created.id, ReviewStatus.APPROVED);
        assertEquals(PublishStatus.DRAFT, approved.publishStatus);
        assertEquals(PublishStatus.PUBLISHED, events.publish(created.id).publishStatus);
    }

    private EventRequest request(String title) {
        EventRequest request = new EventRequest();
        request.title = title;
        request.summary = title + "摘要";
        request.whatHappened = "已确认发生";
        request.whyImportant = "对开发者有价值";
        request.availability = "AVAILABLE";
        request.developerImpact = "可以立即评估";
        request.category = EventCategory.TRY_NOW;
        request.eventTime = LocalDateTime.now();
        return request;
    }
}
