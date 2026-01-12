package com.swimcolor.client;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendRequestDto;
import com.swimcolor.dto.RecommendResponseDto;
import com.swimcolor.exception.ColorMatchException;
import com.swimcolor.exception.CrawlingException;
import com.swimcolor.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Primary
@Slf4j
@Service
@RequiredArgsConstructor
public class FastapiWebClient implements FastapiClient {

    private final WebClient webClient;

    @Value("${crawling.callbackUrl}")
    private String crawlingCallbackUrl;

    @Override
    public CrawlResponseDto crawlSwimsuits(String url) {
        log.info("Crawling swimsuits from URL: {}", url);

        try {
            CrawlRequestDto requestDto = new CrawlRequestDto();
            requestDto.setCrawlingUrl(url);

            CrawlResponseDto response = webClient.post()
                    .uri("/api/crawl/swimsuits")
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(CrawlResponseDto.class)
                    .block();

            log.info("Successfully crawled swimsuits: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Error crawling swimsuits from URL: {}", url, e);
            throw new CrawlingException(ErrorCode.FASTAPI_CONNECTION_FAILED);
        }
    }

    @Override
    public CrawlResponseDto crawlSwimcaps(String url) {
        log.info("Crawling swimcaps from URL: {}", url);

        try {
            CrawlRequestDto requestDto = new CrawlRequestDto();
            requestDto.setCrawlingUrl(url);

            CrawlResponseDto response = webClient.post()
                    .uri("/api/crawl/swimcaps")
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(CrawlResponseDto.class)
                    .block();

            log.info("Successfully crawled swimcaps: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Error crawling swimcaps from URL: {}", url, e);
            throw new CrawlingException(ErrorCode.FASTAPI_CONNECTION_FAILED);
        }
    }

    @Override
    public RecommendResponseDto getRecommendSwimcap(String swimsuitId, List<String> colors) {
        log.info("Getting swimcap recommendations for swimsuit ID: {} with colors: {}", swimsuitId, colors);

        try {
            RecommendRequestDto requestDto = new RecommendRequestDto();
            requestDto.setSwimsuitId(swimsuitId);
            requestDto.setColors(colors);

            RecommendResponseDto response = webClient.post()
                    .uri("/recommend")
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(RecommendResponseDto.class)
                    .block();

            log.info("Successfully retrieved swimcap recommendations: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Error getting swimcap recommendations for swimsuit ID: {}", swimsuitId, e);
            throw new ColorMatchException(ErrorCode.FASTAPI_CONNECTION_FAILED);
        }
    }

    /**
     * 비동기 방식으로 수영복 크롤링 (Reactive)
     */
    @Override
    public Mono<Void> crawlSwimsuitsAsync(String url, Long logId) {
        log.info("Async crawling swimsuits from URL: {}", url);

        CrawlRequestDto requestDto = CrawlRequestDto.builder()
                .logId(logId)
                .crawlingUrl(url)
                .callbackUrl(crawlingCallbackUrl.concat("/swimsuits"))
                .build();

        return webClient.post()
                .uri("/crawl/swimsuits")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response -> log.info("Async swimsuit crawling completed: {}", response))
                .doOnError(error -> log.error("Async swimsuit crawling failed for URL: {}", url, error));
    }

    /**
     * 비동기 방식으로 수모 크롤링 (Reactive)
     */
    @Override
    public Mono<Void> crawlSwimcapsAsync(String url, Long logId) {
        log.info("Async crawling swimcaps from URL: {}", url);

        CrawlRequestDto requestDto = CrawlRequestDto.builder()
                .logId(logId)
                .crawlingUrl(url)
                .callbackUrl(crawlingCallbackUrl.concat("/swimcaps"))
                .build();

        log.info("Async crawling swimcaps from callbackUrl: {}", requestDto.getCallbackUrl());

        return webClient.post()
                .uri("/crawl/swimcaps")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response -> log.info("Async swimcap crawling completed: {}", response))
                .doOnError(error -> log.error("Async swimcap crawling failed for URL: {}", url, error));
    }

    /**
     * 비동기 방식으로 수모 추천 (Reactive)
     */
    public Mono<RecommendResponseDto> getRecommendSwimcapAsync(String swimsuitId, List<String> colors) {
        log.info("Async getting swimcap recommendations for swimsuit ID: {} with colors: {}", swimsuitId, colors);

        RecommendRequestDto requestDto = new RecommendRequestDto();
        requestDto.setSwimsuitId(swimsuitId);
        requestDto.setColors(colors);

        return webClient.post()
                .uri("/api/recommend/swimcap")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(RecommendResponseDto.class)
                .doOnSuccess(response -> log.info("Async swimcap recommendation completed: {}", response))
                .doOnError(error -> log.error("Async swimcap recommendation failed for swimsuit ID: {}", swimsuitId, error));
    }
}
