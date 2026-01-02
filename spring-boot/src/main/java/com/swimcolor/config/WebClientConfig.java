package com.swimcolor.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${fastapi}")
    private String fastapiBaseUrl;

    @Value("${api.fastapi.read-timeout:30000}")
    private int readTimeout;

    @Value("${api.fastapi.connect-timeout:5000}")
    private int connectTimeout;

    @Bean
    public WebClient webClient() {
//        todo 타임아웃초 고민
//        HttpClient httpClient = HttpClient.create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
//                .responseTimeout(Duration.ofMillis(readTimeout))
//                .doOnConnected(conn ->
//                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
//                                .addHandlerLast(new WriteTimeoutHandler(connectTimeout, TimeUnit.MILLISECONDS))
//                );

        return WebClient.builder()
                .baseUrl(fastapiBaseUrl)
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
