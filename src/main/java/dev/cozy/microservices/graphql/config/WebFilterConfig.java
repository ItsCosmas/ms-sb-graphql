package dev.cozy.microservices.graphql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfig {
    @Bean
    public CorrelationIDWebFilter correlationIdWebFilter() {
        return new CorrelationIDWebFilter();
    }
}
