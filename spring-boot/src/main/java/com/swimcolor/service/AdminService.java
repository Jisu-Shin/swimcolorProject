package com.swimcolor.service;

import com.swimcolor.client.FastapiClient;
import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import com.swimcolor.domain.ViewType;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.exception.CrawlingException;
import com.swimcolor.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    // todo adminservice에 의존성이 있는 서비스들이 이렇게 많아도 될까?
    private final FastapiClient fastapiClient;
    private final SwimsuitService swimsuitService;
    private final SwimcapService swimcapService;
    private final CrawlingLogService crawlingLogService;
    private final CrawlStatusService crawlStatusService;
    private final RecentViewLogService recentViewLogService;

    private final String SWIMSUIT_VIEW_LOG_ID = "CRAWL_SWIMSUIT_LOG";
    private final String SWIMCAP_VIEW_LOG_ID = "CRAWL_SWIMCAP_LOG";

    public void crawlSwimsuits(String url) {
        log.info("#### [SWIMSUIT] 크롤링 시작: {}", url);

        if(crawlStatusService.getCrawlStatus(ItemType.SWIMSUIT.name()) == CrawlStatus.RUNNING.name()) {
            throw new CrawlingException(ErrorCode.CRAWLING_ALREADY_IN_PROGRESS);
        }

        // 1. 크롤링 상태 저장
        crawlStatusService.runSwimsuitCrawling();

        // 2. 크롤링 로그 저장
        Long logId = saveLog(url, ItemType.SWIMSUIT);

        // 3. FastAPI 호출
        fastapiClient.crawlSwimsuitsAsync(url, logId)
                .subscribe(
                        success -> {
                            log.info("#### [SWIMSUIT] FastAPI 접수 완료: logId={}", logId);
                        },
                        error -> {
                            log.error("#### [SWIMSUIT] FastAPI 호출 실패: ", error);

                            // 여기서 실패 처리를 직접 해줘야 함!
                            crawlStatusService.failSwimsuitCrawling();
                            crawlingLogService.updateCrawlingLog(logId, CrawlStatus.FAILED, 0, "FastAPI 연결 실패: " + error.getMessage());
                        }
                );
    }

    @Transactional
    public void responseCrawlSwimsuits(CrawlResponseDto crawlResponseDto) {
        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.COMPLETED) {
            crawlStatusService.completeSwimsuitCrawling();

            // 2. DB 저장
            int count = swimsuitService.saveSwimsuit(crawlResponseDto);

            // 3. 성공 로그 저장
            log.info("#### [SWIMSUIT] 저장 완료: {} 건", count);
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.COMPLETED, count, null);

            // 4. 크롤링한 날짜 최근뷰로그 저장하기
            recentViewLogService.save(SWIMSUIT_VIEW_LOG_ID, ViewType.CRAWL_SWIMSUIT);
        }

        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.FAILED) {
            crawlStatusService.failSwimsuitCrawling();

            // 3. 실패 로그 저장
            log.info("#### [SWIMSUIT] fastapi 크롤 결과 실패");
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.FAILED, 0, crawlResponseDto.getErrorMsg());
        }
    }

    public void crawlSwimcaps(String url) {
        log.info("#### [SWIMCAP] 크롤링 시작: {}", url);

        if(crawlStatusService.getCrawlStatus(ItemType.SWIMCAP.name()) == CrawlStatus.RUNNING.name()) {
            throw new CrawlingException(ErrorCode.CRAWLING_ALREADY_IN_PROGRESS);
        }

        // 1. 크롤링 상태 저장
        crawlStatusService.runSwimcapCrawling();

        // 2. 크롤링 로그 저장
        Long logId = saveLog(url, ItemType.SWIMCAP);

        // 3. fastapi 호출
        fastapiClient.crawlSwimcapsAsync(url, logId)
                .subscribe(
                        success -> {
                            log.info("#### [SWIMCAP] FastAPI 접수 완료: logId={}", logId);
                        },
                        error -> {
                            // FastAPI 서버가 죽었거나, 타임아웃 났을 때 실행
                            log.error("#### [SWIMCAP] FastAPI 호출 실패: ", error);

                            // 여기서 실패 처리를 직접 해줘야 함!
                            crawlStatusService.failSwimcapCrawling();
                            crawlingLogService.updateCrawlingLog(logId, CrawlStatus.FAILED, 0, "FastAPI 연결 실패: " + error.getMessage());
                        }
                );
    }

    @Transactional
    public void responseCrawlSwimcaps(CrawlResponseDto crawlResponseDto) {
        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.COMPLETED) {
            crawlStatusService.completeSwimcapCrawling();

            // 2. DB 저장
            int count = swimcapService.saveSwimcap(crawlResponseDto);

            // 3. 성공 로그 저장
            log.info("#### [SWIMCAP] 저장 완료: {} 건", count);
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.COMPLETED, count, null);

            // 4. 크롤링한 날짜 최근뷰로그 저장하기
            recentViewLogService.save(SWIMCAP_VIEW_LOG_ID, ViewType.CRAWL_SWIMCAP);
        }

        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.FAILED) {
            crawlStatusService.failSwimcapCrawling();

            // 3. 실패 로그 저장
            log.info("#### [SWIMCAP] fastapi 크롤 결과 실패");
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.FAILED, 0, crawlResponseDto.getErrorMsg());
        }

    }

    // 로그 저장 로직이 중복되니 별도 메서드로 추출하는 게 깔끔합니다!
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
