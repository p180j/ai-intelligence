package com.aiintelligence.product;

import com.aiintelligence.common.NotFoundException;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/product-candidates")
public class ProductCandidateController {
    private final JdbcTemplate jdbc;private final ProductService products;
    public ProductCandidateController(JdbcTemplate jdbc,ProductService products){this.jdbc=jdbc;this.products=products;}
    @GetMapping public List<ProductCandidate> list(){return jdbc.query("SELECT * FROM ai_product_candidate WHERE status='PENDING' ORDER BY created_at,id",(r,n)->{ProductCandidate c=new ProductCandidate();c.id=r.getLong("id");c.eventId=r.getLong("event_id");c.name=r.getString("name");c.entityType=r.getString("entity_type");c.status=r.getString("status");c.createdAt=r.getTimestamp("created_at").toLocalDateTime();return c;});}
    @PostMapping("/{id}/confirm") @Transactional public Product confirm(@PathVariable long id,@RequestBody ProductCandidateRequest r){ProductCandidate c=find(id);Product p;if(r.productId!=null)p=products.findById(r.productId);else{ProductRequest request=new ProductRequest();request.name=c.name;request.slug=r.slug==null||r.slug.trim().isEmpty()?slug(c.name):r.slug;request.productType=c.entityType;p=products.create(request);}products.attachEvent(p.id,c.eventId,null);jdbc.update("UPDATE ai_product_candidate SET status='CONFIRMED' WHERE id=?",id);return products.findById(p.id);}
    @PostMapping("/{id}/ignore") public void ignore(@PathVariable long id){if(jdbc.update("UPDATE ai_product_candidate SET status='IGNORED' WHERE id=?",id)==0)throw new NotFoundException("Product candidate not found: "+id);}
    private ProductCandidate find(long id){return jdbc.query("SELECT * FROM ai_product_candidate WHERE id=?",(r,n)->{ProductCandidate c=new ProductCandidate();c.id=r.getLong("id");c.eventId=r.getLong("event_id");c.name=r.getString("name");c.entityType=r.getString("entity_type");c.status=r.getString("status");return c;},id).stream().findFirst().orElseThrow(()->new NotFoundException("Product candidate not found: "+id));}
    private String slug(String value){return value.toLowerCase().trim().replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");}
}
