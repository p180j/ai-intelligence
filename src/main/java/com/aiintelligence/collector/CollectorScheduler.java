package com.aiintelligence.collector;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component @ConditionalOnProperty(name="collector.enabled",havingValue="true")
public class CollectorScheduler {private final CollectorService service;public CollectorScheduler(CollectorService service){this.service=service;}@Scheduled(fixedDelayString="${collector.fixed-delay-ms:1800000}")public void run(){service.collectAll();}}
