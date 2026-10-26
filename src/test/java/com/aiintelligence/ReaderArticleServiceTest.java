package com.aiintelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.aiintelligence.ai.*;
import com.aiintelligence.event.*;
import com.aiintelligence.reader.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ReaderArticleServiceTest {
    private ReaderArticleRepository repository;
    private EventService events;
    private AiClient ai;
    private PromptCatalog prompts;
    private PipelineRepository logs;
    private ReaderArticleService service;

    @BeforeEach
    void setUp() {
        repository=mock(ReaderArticleRepository.class);
        events=mock(EventService.class);
        ai=mock(AiClient.class);
        prompts=mock(PromptCatalog.class);
        logs=mock(PipelineRepository.class);
        service=new ReaderArticleService(repository,events,ai,prompts,new com.fasterxml.jackson.databind.ObjectMapper(),logs);
        Event event=new Event();event.id=1L;event.reviewStatus=ReviewStatus.APPROVED;event.title="已审核事件";
        when(events.findById(1L)).thenReturn(event);
        when(prompts.readerArticle()).thenReturn("reader prompt");
        ReaderArticle saved=new ReaderArticle();saved.eventId=1L;saved.promptVersion="reader-v5";
        when(repository.find(1L)).thenReturn(Optional.of(saved));
    }

    @Test
    void savesSectionedReaderArticleAsVersionFive() {
        AiResponse response=new AiResponse();
        response.content="{\"readerTitle\":\"标题\",\"readerLead\":\"导语\",\"readerContent\":\"## 核心变化\\n这项更新的**关键变化**已经确认。\\n## 对开发者的影响\\n第二段。\",\"readerConclusion\":\"值得关注。\"}";
        when(ai.generate(any(AiRequest.class))).thenReturn(response);

        ReaderArticle article=service.generate(1L);

        assertEquals("reader-v5",article.promptVersion);
        verify(repository).save(eq(1L),eq("标题"),eq("导语"),contains("**关键变化**"),eq("值得关注。"),eq("reader-v5"));
        ArgumentCaptor<AiRequest> request=ArgumentCaptor.forClass(AiRequest.class);
        verify(ai).generate(request.capture());
        assertEquals("reader-v5",request.getValue().promptVersion);
    }

    @Test
    void acceptsContinuousProseForShortReaderArticles() {
        AiResponse response=new AiResponse();
        response.content="{\"readerTitle\":\"标题\",\"readerLead\":\"导语\",\"readerContent\":\"第一段。\\n第二段。\",\"readerConclusion\":\"值得关注。\"}";
        when(ai.generate(any(AiRequest.class))).thenReturn(response);

        service.generate(1L);

        verify(repository).save(eq(1L),eq("标题"),eq("导语"),eq("第一段。\n第二段。"),eq("值得关注。"),eq("reader-v5"));
    }
}
