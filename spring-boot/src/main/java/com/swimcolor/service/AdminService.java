package com.swimcolor.service;

import com.swimcolor.client.FastapiClient;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.CrawlingStatus;
import com.swimcolor.domain.ItemType;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendResponseDto;
import com.swimcolor.mapper.ColorMatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final FastapiClient fastapiClient;
    private final SwimsuitService swimsuitService;
    private final SwimcapService swimcapService;
    private final CrawlingLogService crawlingLogService;
    private final CrawlStatusService crawlStatusService;

    @Async
    public void crawlSwimsuits(String url) {
        long startTime = System.currentTimeMillis(); // 소요 시간 측정 시작

        try {
            log.info("#### [SWIMSUIT] 크롤링 시작: {}", url);
            crawlStatusService.runSwimsuitCrawling();

            // 1. FastAPI 호출
            CrawlResponseDto crawlResponseDto = fastapiClient.crawlSwimsuits(url);

            // 2. 크롤 상태 값 변경
            crawlStatusService.completeSwimsuitCrawling();

            // 2. DB 저장
            int count = swimsuitService.saveSwimsuit(crawlResponseDto);

            long duration = System.currentTimeMillis() - startTime; // 소요 시간 계산(ms)

            // 3. 성공 로그 저장
            saveLog(url, count, ItemType.SWIMSUIT, CrawlingStatus.SUCCESS, null, duration);
            log.info("#### [SWIMSUIT] 저장 완료: {} 건, 소요시간: {}ms", count, duration);

        } catch (Exception e) {
            log.error("#### [SWIMSUIT] 크롤링 중 에러 발생: ", e);
            long duration = System.currentTimeMillis() - startTime;

            // 2. 크롤 상태 값 변경
            crawlStatusService.failSwimsuitCrawling();

            // 4. 실패 로그 저장 (에러 메시지 포함)
            saveLog(url, 0, ItemType.SWIMSUIT, CrawlingStatus.FAILURE, e.getMessage(), duration);
        }

    }

    @Async
    public void crawlSwimcaps(String url) {
        long startTime = System.currentTimeMillis(); // 소요 시간 측정 시작

        try {
            log.info("#### [SWIMCAP] 크롤링 시작: {}", url);
            crawlStatusService.runSwimcapCrawling();

            // 1. FastAPI 호출
            CrawlResponseDto crawlResponseDto = fastapiClient.crawlSwimcaps(url);

            // 2. 크롤 상태 값 변경
            crawlStatusService.completeSwimcapCrawling();

            // 2. DB 저장
            int count = swimcapService.saveSwimcap(crawlResponseDto);

            long duration = System.currentTimeMillis() - startTime; // 소요 시간 계산(ms)

            // 3. 성공 로그 저장
            saveLog(url, count, ItemType.SWIMCAP, CrawlingStatus.SUCCESS, null, duration);
            log.info("#### [SWIMCAP] 저장 완료: {} 건, 소요시간: {}ms", count, duration);

        } catch (Exception e) {
            log.error("#### [SWIMCAP] 크롤링 중 에러 발생: ", e);
            long duration = System.currentTimeMillis() - startTime;
            // 2. 크롤 상태 값 변경
            crawlStatusService.failSwimcapCrawling();

            // 4. 실패 로그 저장 (에러 메시지 포함)
            saveLog(url, 0, ItemType.SWIMCAP, CrawlingStatus.FAILURE, e.getMessage(), duration);
        }
    }

    // 로그 저장 로직이 중복되니 별도 메서드로 추출하는 게 깔끔합니다!
    private void saveLog(String url, int count, ItemType type, CrawlingStatus status, String errorMsg, long duration) {
        CrawlingLog crawlingLog = CrawlingLog.builder()
                .sourceUrl(url)
                .totalCount(count)
                .itemType(type)
                .crawledAt(LocalDateTime.now())
                .status(status)
                .errorMessage(errorMsg) // 엔티티에 필드 추가 필요
                .executionTime(duration) // 엔티티에 필드 추가 필요 (ms 단위)
                .build();

        crawlingLogService.saveCrawlingLog(crawlingLog);
    }
}
