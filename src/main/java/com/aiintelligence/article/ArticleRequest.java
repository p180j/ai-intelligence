package com.aiintelligence.article;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ArticleRequest {
    @NotNull public Long sourceId;
    @NotBlank public String title;
    @NotBlank @Size(max = 750) public String url;
    public String author;
    public String language;
    public LocalDateTime publishedAt;
    public String rawContent;
    public String cleanContent;
    public String contentHash;
    public ArticleStatus collectStatus = ArticleStatus.NEW;
    public LocalDateTime collectedAt;
}
