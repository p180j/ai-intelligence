package com.aiintelligence.report;

import java.util.*;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/reports")
public class DailyReportController {
    private final DailyReportService service;public DailyReportController(DailyReportService service){this.service=service;}
    @GetMapping public List<DailyReport> list(){return service.findAll();}
    @GetMapping("/{id}") public DailyReport get(@PathVariable long id){return service.findById(id);}
    @PostMapping("/generate") @ResponseStatus(HttpStatus.CREATED) public DailyReport generate(@Valid @RequestBody DailyReportRequest r){return service.generate(r.reportDate);}
    @PutMapping("/{id}") public DailyReport update(@PathVariable long id,@Valid @RequestBody DailyReportUpdateRequest r){return service.update(id,r);}
    @PostMapping("/{id}/events") public DailyReport add(@PathVariable long id,@Valid @RequestBody ReportEventRequest r){return service.add(id,r);}
    @DeleteMapping("/{id}/events/{eventId}") public DailyReport remove(@PathVariable long id,@PathVariable long eventId){return service.remove(id,eventId);}
    @PostMapping("/{id}/publish") public DailyReport publish(@PathVariable long id){return service.publish(id);}
    @PostMapping("/{id}/offline") public DailyReport offline(@PathVariable long id){return service.offline(id);}
    @PutMapping("/{id}/events/{eventId}/feedback") public DailyReport feedback(@PathVariable long id,@PathVariable long eventId,@RequestBody Map<String,String> r){return service.feedback(id,eventId,r.get("qualityFeedback"));}
    @GetMapping(value="/{id}/wechat",produces="text/plain;charset=UTF-8") public String wechat(@PathVariable long id){return service.wechat(id);}
}
