package com.aiintelligence.ai;

import com.fasterxml.jackson.databind.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenAiClient implements AiClient {
    private final RestTemplate http;private final ObjectMapper mapper;private final String baseUrl,key,model,provider;
    public OpenAiClient(RestTemplateBuilder b,ObjectMapper m,@Value("${ai.base-url:https://api.openai.com/v1}")String u,@Value("${ai.api-key:}")String k,@Value("${ai.model:gpt-4o-mini}")String model,@Value("${ai.provider:openai}")String provider,@Value("${ai.timeout-seconds:45}")long timeout){this.http=b.setConnectTimeout(Duration.ofSeconds(timeout)).setReadTimeout(Duration.ofSeconds(timeout)).build();this.mapper=m;this.baseUrl=u;this.key=k;this.model=model;this.provider=provider;}
    public AiResponse generate(AiRequest r){if(key==null||key.trim().isEmpty())throw new IllegalStateException("AI_API_KEY is not configured");try{long start=System.currentTimeMillis();Map<String,Object> body=new LinkedHashMap<>();body.put("model",model);body.put("temperature",0);body.put("response_format",Collections.singletonMap("type","json_object"));List<Map<String,String>> messages=new ArrayList<>();messages.add(message("system",r.systemPrompt));messages.add(message("user",r.userPrompt));body.put("messages",messages);HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);h.setBearerAuth(key);JsonNode root=mapper.readTree(http.postForObject(baseUrl+"/chat/completions",new HttpEntity<>(body,h),String.class));AiResponse out=new AiResponse();out.content=root.path("choices").path(0).path("message").path("content").asText();out.provider=provider;out.model=model;out.inputTokens=root.path("usage").path("prompt_tokens").isNumber()?root.path("usage").path("prompt_tokens").asInt():null;out.outputTokens=root.path("usage").path("completion_tokens").isNumber()?root.path("usage").path("completion_tokens").asInt():null;out.latencyMs=System.currentTimeMillis()-start;return out;}catch(Exception e){throw new IllegalStateException("AI request failed: "+e.getMessage(),e);}}
    private Map<String,String> message(String role,String content){Map<String,String> m=new LinkedHashMap<>();m.put("role",role);m.put("content",content);return m;}
}
