package com.aiintelligence.collector;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class UrlNormalizer {
    private static final Set<String> TRACKING=new HashSet<>(Arrays.asList("utm_source","utm_medium","utm_campaign","utm_content","utm_term"));
    public String normalize(String value) {
        try { URI uri=new URI(value.trim()); List<String> kept=new ArrayList<>();
            if(uri.getRawQuery()!=null) for(String part:uri.getRawQuery().split("&")){String key=part.split("=",2)[0].toLowerCase();if(!TRACKING.contains(key))kept.add(part);}
            String query=kept.isEmpty()?null:String.join("&",kept);int port=(uri.getScheme()!=null&&((uri.getScheme().equalsIgnoreCase("http")&&uri.getPort()==80)||(uri.getScheme().equalsIgnoreCase("https")&&uri.getPort()==443)))?-1:uri.getPort();
            return new URI(uri.getScheme()==null?null:uri.getScheme().toLowerCase(),uri.getUserInfo(),uri.getHost()==null?null:uri.getHost().toLowerCase(),port,uri.getPath(),query,null).toString();
        } catch(Exception e){throw new IllegalArgumentException("Invalid article URL: "+value);}
    }
}
