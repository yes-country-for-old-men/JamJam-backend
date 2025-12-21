package com.jamjam.global.config;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class GeminiConfig {

    @Bean
    @Qualifier("geminiWebClient")
    public WebClient geminiWebClient(
            @Value("${gemini.api.base-url}") String baseUrl,
            @Value("${gemini,api.key}") String apiKey
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-goog-api-key", apiKey)
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create().responseTimeout(Duration.ofSeconds(60))
                        )
                )
                .build();
    }
}
