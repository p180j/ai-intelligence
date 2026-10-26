package com.aiintelligence.web;

import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {
    private final boolean totpEnabled;
    public PageController(@Value("${admin.totp-enabled:true}") boolean totpEnabled){this.totpEnabled=totpEnabled;}
    @GetMapping("/login") public String login(){return "forward:/login.html";}
    @GetMapping("/api/public/login-config") @ResponseBody public Map<String,Boolean> loginConfig(){return Collections.singletonMap("totpEnabled",totpEnabled);}
    @GetMapping("/admin") public String admin(){return "forward:/admin/index.html";}
    @GetMapping("/event/{id}") public String event(){return "forward:/event.html";}
    @GetMapping("/product/{slug}") public String product(){return "forward:/product.html";}
    @GetMapping("/search") public String search(){return "forward:/search.html";}
}
