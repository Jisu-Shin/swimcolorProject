package com.swimcolor.service;

import com.swimcolor.client.FastapiClient;
import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import com.swimcolor.dto.CrawlResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

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
        log.info("#### [SWIMSUIT] 크롤링 시작: {}", url);

        // 1. 크롤링 로그 저장
        Long logId = saveLog(url, ItemType.SWIMSUIT);

        crawlStatusService.runSwimsuitCrawling();

        // 1. FastAPI 호출
//            CrawlResponseDto crawlResponseDto = fastapiClient.crawlSwimsuits(url);
        fastapiClient.crawlSwimsuitsAsync(url, logId)
                .subscribe(
                        success -> {
                            // FastAPI가 "알겠어!"라고 대답했을 때 실행
                            log.info("#### [SWIMSUIT] FastAPI 접수 완료: logId={}", logId);
                        },
                        error -> {
                            // FastAPI 서버가 죽었거나, 타임아웃 났을 때 실행
                            log.error("#### [SWIMSUIT] FastAPI 호출 실패: ", error);

                            // 여기서 실패 처리를 직접 해줘야 함!
                            crawlStatusService.failSwimsuitCrawling();
                            crawlingLogService.updateCrawlingLog(logId, CrawlStatus.FAILED, 0, "FastAPI 연결 실패: " + error.getMessage());
                        }
                );
    }

    public void responseCrawlSwimsuits(CrawlResponseDto crawlResponseDto) {
        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.COMPLETED) {
            crawlStatusService.completeSwimsuitCrawling();

            // 2. DB 저장
            int count = swimsuitService.saveSwimsuit(crawlResponseDto);

            // 3. 성공 로그 저장
            log.info("#### [SWIMSUIT] 저장 완료: {} 건", count);
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.COMPLETED, count, null);
        }

        if (crawlResponseDto.getCrawlStatus() == CrawlStatus.FAILED) {
            crawlStatusService.failSwimsuitCrawling();

            // 3. 실패 로그 저장
            log.info("#### [SWIMSUIT] fastapi 크롤 결과 실패");
            crawlingLogService.updateCrawlingLog(crawlResponseDto.getLogId(), CrawlStatus.FAILED, 0, crawlResponseDto.getErrorMsg());
        }

    }

    @Async
    public void crawlSwimcaps(String url) {
        long startTime = System.currentTimeMillis(); // 소요 시간 측정 시작
        String jobId = UUID.randomUUID().toString();

        Long crawlingLogId = saveLog(url, ItemType.SWIMCAP);

        try {
            log.info("#### [SWIMCAP] 크롤링 시작: {}", url);

            // 1. FastAPI 호출
            CrawlResponseDto crawlResponseDto = fastapiClient.crawlSwimcaps(url);

            // 2. 크롤 상태 값 변경
            crawlStatusService.completeSwimcapCrawling();

            // 2. DB 저장
            int count = swimcapService.saveSwimcap(crawlResponseDto);

            long duration = System.currentTimeMillis() - startTime; // 소요 시간 계산(ms)

            // 3. 성공 로그 저장
            log.info("#### [SWIMCAP] 저장 완료: {} 건, 소요시간: {}ms", count, duration);

        } catch (Exception e) {
            log.error("#### [SWIMCAP] 크롤링 중 에러 발생: ", e);
            long duration = System.currentTimeMillis() - startTime;
            // 2. 크롤 상태 값 변경
            crawlStatusService.failSwimcapCrawling();

            // 4. 실패 로그 저장 (에러 메시지 포함)
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
