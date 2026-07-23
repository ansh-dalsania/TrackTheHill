package com.trackthehill.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the WebClient used to call the Congress.gov API.
 * Base URL and API key are pulled from application.properties (pulls from the
 * CONGRESS_API_KEY environment variable).
 */
@Configuration
public class WebClientConfig {

    @Value("${congress.api.key}")
    private String apiKey;

    @Bean
    public WebClient congressApiClient() {
        return WebClient.builder()
                .baseUrl("https://api.congress.gov/v3")
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }

    @Value("${fec.api.key}")
    private String fecApiKey;

    @Bean
    public WebClient fecApiClient() {
        return WebClient.builder()
                .baseUrl("https://api.open.fec.gov/v1")
                .build();
    }

    public String getFecApiKey() {
        return fecApiKey;
    }
}