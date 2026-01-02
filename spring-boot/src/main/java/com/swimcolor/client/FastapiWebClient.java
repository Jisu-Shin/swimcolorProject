package com.swimcolor.client;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendRequestDto;
import com.swimcolor.dto.RecommendResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public CrawlResponseDto crawlSwimsuits(String url) {
        // todo 파라미터 변경 필요 예상 url -> CrawlRequestDto
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
            throw new RuntimeException("Failed to crawl swimsuits: " + e.getMessage(), e);
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
            throw new RuntimeException("Failed to crawl swimcaps: " + e.getMessage(), e);
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
                    .uri("/api/recommend/swimcap")
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(RecommendResponseDto.class)
                    .block();

            log.info("Successfully retrieved swimcap recommendations: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Error getting swimcap recommendations for swimsuit ID: {}", swimsuitId, e);
            throw new RuntimeException("Failed to get swimcap recommendations: " + e.getMessage(), e);
        }
    }

    /**
     * 비동기 방식으로 수영복 크롤링 (Reactive)
     */
    @Override
    public Mono<CrawlResponseDto> crawlSwimsuitsAsync(String url, Long logId) {
        log.info("Async crawling swimsuits from URL: {}", url);

        CrawlRequestDto requestDto = new CrawlRequestDto();
        requestDto.setCrawlingUrl(url);
        requestDto.setLogId(logId);
        // todo 환경변수 필요
        requestDto.setCallbackUrl("https://localhost:8080/api/crawling/callback/swimsuits");

        return webClient.post()
                .uri("/api/crawl/swimsuits")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(CrawlResponseDto.class)
                .doOnSuccess(response -> log.info("Async swimsuit crawling completed: {}", response))
                .doOnError(error -> log.error("Async swimsuit crawling failed for URL: {}", url, error));
    }

    /**
     * 비동기 방식으로 수모 크롤링 (Reactive)
     */
    public Mono<CrawlResponseDto> crawlSwimcapAsync(String url) {
        log.info("Async crawling swimcaps from URL: {}", url);

        CrawlRequestDto requestDto = new CrawlRequestDto();
        requestDto.setCrawlingUrl(url);

        return webClient.post()
                .uri("/api/crawl/swimcaps")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(CrawlResponseDto.class)
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
