package com.aiintelligence.source;

import com.aiintelligence.collector.CollectorService;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sources")
public class SourceController {
    private final SourceService service;
    private final CollectorService collector;

    public SourceController(SourceService service, CollectorService collector) { this.service = service;this.collector = collector; }

    @GetMapping public List<Source> list() { return service.findAll(); }
    @GetMapping("/{id}") public Source get(@PathVariable long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Source create(@Valid @RequestBody SourceRequest request) { return service.create(request); }
    @PutMapping("/{id}") public Source update(@PathVariable long id, @Valid @RequestBody SourceRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void disable(@PathVariable long id) { service.disable(id); }
    @PostMapping("/{id}/enable") public Source enable(@PathVariable long id) { return service.enable(id); }
    @PostMapping("/{id}/test") public Map<String,Object> test(@PathVariable long id) { return collector.test(id); }
    @DeleteMapping("/{id}/permanent") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable long id) { service.delete(id); }
}

