package com.aiintelligence.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {
    @GetMapping("/login") public String login(){return "forward:/login.html";}
    @GetMapping("/admin") public String admin(){return "forward:/admin/index.html";}
    @GetMapping("/event/{id}") public String event(){return "forward:/event.html";}
    @GetMapping("/product/{slug}") public String product(){return "forward:/product.html";}
    @GetMapping("/search") public String search(){return "forward:/search.html";}
}
