package com.aiintelligence.event;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/events")
public class EventController {
    private final EventService service; public EventController(EventService service){this.service=service;}
    @GetMapping public List<Event> list(){return service.findAll();}
    @GetMapping("/{id}") public Event get(@PathVariable long id){return service.findById(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Event create(@Valid @RequestBody EventRequest r){return service.create(r);}
    @PutMapping("/{id}") public Event update(@PathVariable long id,@Valid @RequestBody EventRequest r){return service.update(id,r);}
    @PostMapping("/{eventId}/articles/{articleId}") public Event attach(@PathVariable long eventId,@PathVariable long articleId,@RequestBody(required=false) ArticleRelationRequest r){return service.attachArticle(eventId,articleId,r);}
    @DeleteMapping("/{eventId}/articles/{articleId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void detach(@PathVariable long eventId,@PathVariable long articleId){service.detachArticle(eventId,articleId);}
}

