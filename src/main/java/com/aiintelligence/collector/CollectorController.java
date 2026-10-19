package com.aiintelligence.collector;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/collect")
public class CollectorController {private final CollectorService service;public CollectorController(CollectorService service){this.service=service;}@PostMapping("/run")public Map<Long,Integer> run(){return service.collectAll();}@PostMapping("/source/{id}")public int source(@PathVariable long id){return service.collect(id);}}
