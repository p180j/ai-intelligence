package com.aiintelligence.product;

import javax.validation.constraints.NotBlank;

public class ProductRequest {
    @NotBlank public String name;
    @NotBlank public String slug;
    public String summary;
    public String productType;
    public String companyName;
    public String officialUrl;
    public String githubUrl;
    public String docsUrl;
    public String apiUrl;
    public Boolean openSource = false;
    public String availability;
    public String capabilityTags;
}
