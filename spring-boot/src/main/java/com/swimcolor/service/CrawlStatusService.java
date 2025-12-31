package com.swimcolor.service;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CrawlStatusService {
    // 작업 상태를 담을 저장소 (key: itemType, value: CrawlStatus)
    private final Map<String, String> crawlStatus = new ConcurrentHashMap<>();

    public void runSwimsuitCrawling() {
        crawlStatus.put(ItemType.SWIMSUIT.name(), CrawlStatus.RUNNING.name());
    }

    public void completeSwimsuitCrawling() {
        crawlStatus.put(ItemType.SWIMSUIT.name(), CrawlStatus.COMPLETED.name());
    }

    public void failSwimsuitCrawling() {
        crawlStatus.put(ItemType.SWIMSUIT.name(), CrawlStatus.FAILED.name());
    }

    public void runSwimcapCrawling() {
        crawlStatus.put(ItemType.SWIMCAP.name(), CrawlStatus.RUNNING.name());
    }

    public void completeSwimcapCrawling() {
        crawlStatus.put(ItemType.SWIMCAP.name(), CrawlStatus.COMPLETED.name());
    }

    public void failSwimcapCrawling() {
        crawlStatus.put(ItemType.SWIMCAP.name(), CrawlStatus.FAILED.name());
    }

    // 상태 확인 API를 위한 메서드
    public String getCrawlStatus(String itemType) {
        return crawlStatus.getOrDefault(itemType, "IDLE");
    }
}
