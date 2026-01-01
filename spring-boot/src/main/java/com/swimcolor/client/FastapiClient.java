package com.swimcolor.client;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendRequestDto;
import com.swimcolor.dto.RecommendResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastapiClient {
    private final RestClient fastApiRestClient;

    /**
     * 수영복을 크롤링 한다
     */
    public CrawlResponseDto crawlSwimsuits(String url) {
        CrawlRequestDto requestDto = new CrawlRequestDto();
        requestDto.setUrl(url);

        return fastApiRestClient.post()
                .uri("/crawl/swimsuits")
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("fastapi-service의 crawlSwimsuits 호출 중 에러 발생: {}", response.getStatusCode());
                    throw new IllegalStateException("FastAPI 수영복 크롤링 실패");
                })
                .body(CrawlResponseDto.class);
    }

    /**
     * 수모를 크롤링 한다
     */
    public CrawlResponseDto crawlSwimcaps(String url) {
        CrawlRequestDto requestDto = new CrawlRequestDto();
        requestDto.setUrl(url);

        return fastApiRestClient.post()
                .uri("/crawl/swimcaps")
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("fastapi-service 호출 실패 - URL: {}, 상태코드: {}", url, response.getStatusCode());
                    throw new IllegalStateException("FastAPI 수모 크롤링 통신 중 상태 오류 발생");
                })
                .body(CrawlResponseDto.class);
    }

    /**
     * 수영복 관련 데이터를 넘기면 수모를 추천받는다
     */
    public RecommendResponseDto getRecommendSwimcap(String swimsuitId, List<String> colors) {
        RecommendRequestDto requestDto = new RecommendRequestDto();
        requestDto.setSwimsuitId(swimsuitId);
        requestDto.setColors(colors);

        return fastApiRestClient.post()
                .uri("/recommend")
                .body(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("fastapi-service의 getRecommendSwimcap 호출 중 에러 발생: {}", response.getStatusCode());
                    throw new IllegalStateException("FastAPI 추천 서비스 호출 실패");
                })
                .body(RecommendResponseDto.class);
    }
}

