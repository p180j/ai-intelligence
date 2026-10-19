package com.aiintelligence.source;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sources")
public class SourceController {
    private final SourceService service;

    public SourceController(SourceService service) { this.service = service; }

    @GetMapping public List<Source> list() { return service.findAll(); }
    @GetMapping("/{id}") public Source get(@PathVariable long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Source create(@Valid @RequestBody SourceRequest request) { return service.create(request); }
    @PutMapping("/{id}") public Source update(@PathVariable long id, @Valid @RequestBody SourceRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void disable(@PathVariable long id) { service.disable(id); }
}

