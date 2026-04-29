package com.reply.camunda.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BiProClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}