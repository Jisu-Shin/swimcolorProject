package com.swimcolor.service;

import com.swimcolor.client.ApiClient;
import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.exception.CrawlingException;
import com.swimcolor.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CrawlingService {
    private final ApiClient apiClient;
    private final CrawlingLogService crawlingLogService;
    private final CrawlingStateManager crawlingStateManager;
    private final RecentViewLogService recentViewLogService;
    private final Map<ItemType, ItemService> itemServiceMap;

    public CrawlingService(ApiClient apiClient, CrawlingLogService crawlingLogService, CrawlingStateManager crawlingStateManager, RecentViewLogService recentViewLogService
    , List<ItemService> itemServices) {
        this.apiClient = apiClient;
        this.crawlingLogService = crawlingLogService;
        this.crawlingStateManager = crawlingStateManager;
        this.recentViewLogService = recentViewLogService;
        this.itemServiceMap = itemServices.stream()
                .collect(Collectors.toMap(ItemService::getItemType, s -> s));
    }

    /**
     * 크롤링 시작
     * @param itemType 크롤링 상품 종류
     * @param url 크롤링 대상 URL
     */
    public void startCrawling(ItemType itemType, String url) {
        log.info("#### [{}] 크롤링 시작: {}", itemType, url);

        // 1. 크롤링 동작 여부 확인
        validateCrawlingRunning(itemType);

        // 2. 크롤링 상태 저장
        crawlingStateManager.runCrawling(itemType);

        // 3. 크롤링 로그 저장
        Long logId = saveLog(itemType, url);

        // 2. 람다 호출
        try {
            apiClient.crawlProducts(url, logId, itemType);
        } catch (RuntimeException e) {
            failCrawlingResponse(itemType, logId, "람다 연결 실패: " + e.getMessage());
        }
    }

    private void validateCrawlingRunning(ItemType itemType) {
        if (crawlingStateManager.isRunning(itemType)) {
            throw new CrawlingException(ErrorCode.CRAWLING_ALREADY_IN_PROGRESS);
        }
    }

    private Long saveLog(ItemType type, String url) {
        CrawlingLog crawlingLog = CrawlingLog.builder()
                .sourceUrl(url)
                .itemType(type)
                .crawledAt(LocalDateTime.now())
                .status(CrawlStatus.RUNNING)
                .build();

        return crawlingLogService.saveCrawlingLog(crawlingLog);
    }

    /**
     * 크롤링 결과 응답
     * @param itemType 크롤링 상품 종류
     * @param crawlResponseDto 크롤링 응답 DTO
     */
    public void handleCrawlingResponse(ItemType itemType, CrawlResponseDto crawlResponseDto) {
        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.COMPLETED) {
            completeCrawlingResponse(itemType, crawlResponseDto);
        }

        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.FAILED) {
            failCrawlingResponse(itemType, crawlResponseDto.getLogId(), crawlResponseDto.getErrorMsg());
        }
    }

    private void completeCrawlingResponse(ItemType itemType, CrawlResponseDto crawlResponseDto) {
        crawlingStateManager.completeCrawling(itemType);

        // 2. DB 저장
        ItemService itemService = itemServiceMap.get(itemType);
        int count = itemService.save(crawlResponseDto);

        // 3. 성공 로그 저장
        log.info("#### [{}}] 저장 완료: {} 건", itemType, count);
        crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.COMPLETED, count, null);

        // 4. 크롤링한 날짜 최근뷰로그 저장하기
        recentViewLogService.saveCrawlingLog(itemType);
    }

    private void failCrawlingResponse(ItemType itemType, Long logId, String errorMsg) {
        crawlingStateManager.failCrawling(itemType);

        // 3. 실패 로그 저장
        log.info("#### [{}}] lambda 크롤링 실패", itemType);
        crawlingLogService.updateCrawlingLog(logId, CrawlStatus.FAILED, 0, errorMsg);
    }

    public void removeCrawlingStatus(ItemType itemType) {
        CrawlingLog lastLog;
        if (ItemType.SWIMCAP == itemType) {
            crawlingStateManager.removeCrawling(ItemType.SWIMCAP);
            lastLog = crawlingLogService.getLastSwimcapCrawlingLog(ItemType.SWIMCAP);

        } else {
            crawlingStateManager.removeCrawling(ItemType.SWIMSUIT);
            lastLog = crawlingLogService.getLastSwimcapCrawlingLog(ItemType.SWIMSUIT);
        }

        crawlingLogService.updateCrawlingLog(lastLog.getId(), CrawlStatus.FAILED, 0, "ADMIN REQUEST FAILED");
    }
}
