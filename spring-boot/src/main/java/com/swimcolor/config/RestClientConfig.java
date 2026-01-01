package com.swimcolor.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${fastapi}")
    private String fastApiUrl;

    @Value("${api.fastapi.read-timeout}")
    private int readTimeout;

    @Value("${api.fastapi.connect-timeout}")
    private int connectTimeout;

    @Bean
    public RestClient fastApiRestClient() {
        // Apache HC5 상세 설정
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                        .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
                        .build())
                .build();

        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .baseUrl(fastApiUrl)
                .build();
    }
}