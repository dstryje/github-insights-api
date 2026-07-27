package com.stryjewski.githubinsights.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${githubapi.base-url}")
    private String baseUrl;

    @Value("${githubapi.accept}")
    private String acceptHeader;

    @Value("${githubapi.version}")
    private String apiVersion;

    @Value("${githubapi.user-agent}")
    private String userAgent;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", acceptHeader)
                .defaultHeader("X-GitHub-Api-Version", apiVersion)
                .defaultHeader("User-Agent", userAgent)
                .build();

    }
}
