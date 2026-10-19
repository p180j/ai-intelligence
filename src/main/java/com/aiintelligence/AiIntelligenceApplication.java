package com.aiintelligence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiIntelligenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiIntelligenceApplication.class, args);
    }
}
