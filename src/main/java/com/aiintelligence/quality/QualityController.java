package com.aiintelligence.quality;

import java.time.LocalDate;
import java.util.*;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/quality")
public class QualityController {
    private final QualityService service;public QualityController(QualityService service){this.service=service;}
    @GetMapping("/events/{id}") public EventFeedback event(@PathVariable long id){return service.find(id);}
    @PutMapping("/events/{id}") public EventFeedback save(@PathVariable long id,@Valid @RequestBody EventFeedbackRequest r){return service.save(id,r);}
    @GetMapping("/eval-cases") public List<EvalCase> cases(){return service.evalCases();}
    @PostMapping("/eval-cases") @ResponseStatus(HttpStatus.CREATED) public EvalCase add(@Valid @RequestBody EvalCaseRequest r){return service.addEval(r);}
    @GetMapping("/daily") public Map<String,Object> daily(@RequestParam @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date){return service.stats(date);}
}
