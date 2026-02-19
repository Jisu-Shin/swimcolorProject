package com.swimcolor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;

@Configuration
public class ClientConfig {

    @Value("${fastapi}")
    private String fastApiUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(fastApiUrl)
                .build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(fastApiUrl)
                .build();
    }

    @Bean
    public LambdaAsyncClient lambdaAsyncClient() {
        return LambdaAsyncClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }
}