package com.aiintelligence.product;

import com.aiintelligence.common.NotFoundException;
import com.aiintelligence.event.PipelineRepository;
import com.aiintelligence.event.EventService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository repository;private final EventService eventService;private final PipelineRepository pipeline;
    public ProductService(ProductRepository repository,EventService eventService,PipelineRepository pipeline){this.repository=repository;this.eventService=eventService;this.pipeline=pipeline;}
    public List<Product> findAll(){List<Product> products=repository.findAll();products.forEach(p->p.events=repository.findEvents(p.id));return products;}
    public Product findById(long id){Product p=repository.findById(id).orElseThrow(()->new NotFoundException("Product not found: "+id));p.events=repository.findEvents(id);return p;}
    public Product findBySlug(String slug){Product p=repository.findBySlug(slug).orElseThrow(()->new NotFoundException("Product not found: "+slug));p.events=repository.findEvents(p.id);return p;}
    public Product findPublishedBySlug(String slug){Product p=repository.findBySlug(slug).orElseThrow(()->new NotFoundException("Product not found: "+slug));p.events=repository.findPublishedEvents(p.id);return p;}
    public List<Product> search(String q){List<Product> list=repository.search(q);list.forEach(p->p.events=repository.findEvents(p.id));return list;}
    @Transactional public Product create(ProductRequest r){long id=repository.insert(r);pipeline.reconcileProductCandidates();return findById(id);}
    @Transactional public Product update(long id,ProductRequest r){if(repository.update(id,r)==0)throw new NotFoundException("Product not found: "+id);pipeline.reconcileProductCandidates();return findById(id);}
    @Transactional public Product attachEvent(long productId,long eventId,ProductEventRequest r){findById(productId);eventService.findById(eventId);String type=r==null||r.relationType==null||r.relationType.trim().isEmpty()?"UPDATE":r.relationType.trim();repository.attachEvent(productId,eventId,type);return findById(productId);}
    @Transactional public int reconcileKnownCandidates(){return pipeline.reconcileProductCandidates();}
}
