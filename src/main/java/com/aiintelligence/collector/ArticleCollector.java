package com.aiintelligence.collector;

import com.aiintelligence.source.Source;

public interface ArticleCollector {
    boolean supports(Source source);
    CollectResult collect(Source source);
}
