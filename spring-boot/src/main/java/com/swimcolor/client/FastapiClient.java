package com.swimcolor.client;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.SimilarResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastapiClient {
    private final RestTemplate restTemplate;

    @Value("${fastapi}")
    private String baseUrl;

    public CrawlResponseDto crawlSwimsuits(String url) {
        CrawlRequestDto requestDto = new CrawlRequestDto();
        requestDto.setUrl(url);
        try {
            ResponseEntity<CrawlResponseDto> response = restTemplate.postForEntity(
                    baseUrl + "/crawl/swimsuits",
                    requestDto,
                    CrawlResponseDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("fastapi-service의 crawlProducts 호출 중 에러 발생", e);
            throw new IllegalStateException();
        }
    }

    public CrawlResponseDto crawlSwimcaps(String url) {
        CrawlRequestDto requestDto = new CrawlRequestDto();
        requestDto.setUrl(url);
        try {
            ResponseEntity<CrawlResponseDto> response = restTemplate.postForEntity(
                    baseUrl + "/crawl/swimcaps",
                    requestDto,
                    CrawlResponseDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("fastapi-service의 crawlProducts 호출 중 에러 발생", e);
            throw new IllegalStateException();
        }
    }

    public SimilarResponseDto getSimilarSwimCap(String swimsuitId) {
        try {
            ResponseEntity<SimilarResponseDto> response = restTemplate.postForEntity(
                    baseUrl + "/crawl",
                    swimsuitId,
                    SimilarResponseDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("fastapi-service의 getSimilarSwimCap 호출 중 에러 발생", e);
            throw new IllegalStateException();
        }
    }
}

