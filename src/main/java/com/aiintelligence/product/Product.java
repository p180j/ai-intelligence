package com.aiintelligence.product;

import java.time.LocalDateTime;
import java.util.List;

public class Product {
    public Long id;
    public String name;
    public String slug;
    public String summary;
    public String productType;
    public String companyName;
    public String officialUrl;
    public String githubUrl;
    public String docsUrl;
    public String apiUrl;
    public Boolean openSource;
    public String availability;
    public String capabilityTags;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public List<ProductEvent> events;
}
