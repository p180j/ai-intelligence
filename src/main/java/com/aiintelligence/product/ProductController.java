package com.aiintelligence.product;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/products")
public class ProductController {
    private final ProductService service;public ProductController(ProductService service){this.service=service;}
    @GetMapping public List<Product> list(){return service.findAll();}
    @GetMapping("/{id}") public Product get(@PathVariable long id){return service.findById(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Product create(@Valid @RequestBody ProductRequest r){return service.create(r);}
    @PutMapping("/{id}") public Product update(@PathVariable long id,@Valid @RequestBody ProductRequest r){return service.update(id,r);}
    @PostMapping("/reconcile") public int reconcile(){return service.reconcileKnownCandidates();}
    @PostMapping("/{productId}/events/{eventId}") public Product attach(@PathVariable long productId,@PathVariable long eventId,@RequestBody(required=false) ProductEventRequest r){return service.attachEvent(productId,eventId,r);}
}

