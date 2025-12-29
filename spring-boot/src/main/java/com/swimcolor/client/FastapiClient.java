package com.swimcolor.client;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendRequestDto;
import com.swimcolor.dto.RecommendResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastapiClient {
    private final RestTemplate restTemplate;

    @Value("${fastapi}")
    private String baseUrl;

    /**
     * 수영복을 크롤링 한다
     * @param url
     * @return
     */
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

    /**
     * 수모를 크롤링 한다
     * @param url
     * @return
     */
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

    /**
     * 수영복 관련 데이터를 넘기면 수모를 추천받는다
     * @param swimsuitId
     * @return
     */
    public RecommendResponseDto getRecommendSwimcap(String swimsuitId, List<String> colors) {
        RecommendRequestDto requestDto = new RecommendRequestDto();
        requestDto.setSwimsuitId(swimsuitId);
        requestDto.setColors(colors);

        try {
            ResponseEntity<RecommendResponseDto> response = restTemplate.postForEntity(
                    baseUrl + "/recommend",
                    requestDto,
                    RecommendResponseDto.class
            );
//            log.info("Raw 응답: {}", response.getBody());  // 실제 JSON 확인
            return response.getBody();
        } catch (Exception e) {
            log.error("fastapi-service의 getRecommendSwimcap 호출 중 에러 발생", e);
            throw new IllegalStateException();
        }
    }
}

