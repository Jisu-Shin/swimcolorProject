package com.swimcolor.service;

import com.swimcolor.domain.ItemType;
import com.swimcolor.dto.CrawlResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    // todo Transactional 은 어떻게 처리할까?
    private final CrawlingOrchestrator crawlingOrchestrator;

    public void crawlSwimsuits(String url) {
        log.info("#### [ADMIN] 수영복 크롤링 요청: {}", url);
        crawlingOrchestrator.startCrawling(ItemType.SWIMSUIT, url);
    }

    public void crawlSwimcaps(String url) {
        log.info("#### [ADMIN] 수모 크롤링 요청: {}", url);
        crawlingOrchestrator.startCrawling(ItemType.SWIMCAP, url);
    }

    @Transactional
    public void responseCrawlSwimsuits(CrawlResponseDto crawlResponseDto) {
        log.info("#### [ADMIN] 수영복 크롤링 응답 처리 - logId : {}", crawlResponseDto.getLogId());
        crawlingOrchestrator.handleCrawlingResponse(ItemType.SWIMSUIT, crawlResponseDto);
    }

    @Transactional
    public void responseCrawlSwimcaps(CrawlResponseDto crawlResponseDto) {
        log.info("#### [ADMIN] 수모 크롤링 응답 처리 - logId : {}", crawlResponseDto.getLogId());
        crawlingOrchestrator.handleCrawlingResponse(ItemType.SWIMCAP, crawlResponseDto);
    }
}
