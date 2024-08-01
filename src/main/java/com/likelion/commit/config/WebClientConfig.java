package com.likelion.commit.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Configuration
public class WebClientConfig {

    @Value("${gpt.api.key}")
    private final String apiKey;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

    }
}
