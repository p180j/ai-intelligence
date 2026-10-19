package com.aiintelligence.collector;

import java.util.ArrayList;
import java.util.List;

public class CollectResult {
    public List<ArticleCandidate> articles = new ArrayList<>();
    public static CollectResult of(List<ArticleCandidate> articles) { CollectResult r=new CollectResult();r.articles=articles;return r; }
}
