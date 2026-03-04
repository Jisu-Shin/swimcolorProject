package com.swimcolor.service;

import com.swimcolor.client.ApiClient;
import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import com.swimcolor.domain.ViewType;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.exception.CrawlingException;
import com.swimcolor.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingOrchestrator {
    // todo adminservice에 의존성이 있는 서비스들이 이렇게 많아도 될까?
    private final ApiClient apiClient;
    private final CrawlingLogService crawlingLogService;
    private final CrawlingStateManager crawlingStateManager;
    private final RecentViewLogService recentViewLogService;

    private final String SWIMSUIT_VIEW_LOG_ID = "CRAWL_SWIMSUIT_LOG";
    private final String SWIMCAP_VIEW_LOG_ID = "CRAWL_SWIMCAP_LOG";


    public void startCrawling(ItemType itemType, String url) {
        log.info("#### [{}] 크롤링 시작: {}", itemType, url);

        // 1. 크롤링 진행 중인지 유효성 검사
        validateCrawlingRunning(itemType);

        // 2. 크롤링 상태 저장
        crawlingStateManager.runCrawling(itemType);

        // 3. 크롤링 로그 저장
        Long logId = saveLog(url, itemType);

        // 4. 람다 호출
        try {
            apiClient.crawlProducts(url, logId, itemType);
        } catch (RuntimeException e) {
            crawlingStateManager.failCrawling(itemType);
            crawlingLogService.updateCrawlingLog(logId, CrawlStatus.FAILED, 0, "람다 연결 실패: " + e.getMessage());
        }
    }

    private void validateCrawlingRunning(ItemType itemType) {
        if (crawlingStateManager.isRunning(itemType)) {
            throw new CrawlingException(ErrorCode.CRAWLING_ALREADY_IN_PROGRESS);
        }
    }

    public void handleCrawlingResponse(ItemType itemType, CrawlResponseDto crawlResponseDto) {
        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.COMPLETED) {
            crawlingStateManager.completeCrawling(itemType);

            // 2. DB 저장
            int count = swimsuitService.saveSwimsuit(crawlResponseDto);

            // 3. 성공 로그 저장
            log.info("#### [SWIMSUIT] 저장 완료: {} 건", count);
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.COMPLETED, count, null);

            // 4. 크롤링한 날짜 최근뷰로그 저장하기
            recentViewLogService.save(SWIMSUIT_VIEW_LOG_ID, ViewType.CRAWL_SWIMSUIT);
        }

        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.FAILED) {
            crawlingStateManager.failCrawling(itemType);

            // 3. 실패 로그 저장
            log.info("#### [{}}] lambda 크롤링 실패", itemType);
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.FAILED, 0, crawlResponseDto.getErrorMsg());
        }
    }

    private Long saveLog(String url, ItemType type) {
        CrawlingLog crawlingLog = CrawlingLog.builder()
                .sourceUrl(url)
                .itemType(type)
                .crawledAt(LocalDateTime.now())
                .status(CrawlStatus.RUNNING)
                .build();

        return crawlingLogService.saveCrawlingLog(crawlingLog);
    }
}
