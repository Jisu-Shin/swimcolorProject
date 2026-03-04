package com.swimcolor.service;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.ItemType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CrawlingStateManager {
    private final Map<ItemType, CrawlStatus> crawlingStatusMap = new ConcurrentHashMap<>();

    public void runCrawling(ItemType itemType) {
        crawlingStatusMap.put(itemType, CrawlStatus.RUNNING);
    }

    public void completeCrawling(ItemType itemType) {
        crawlingStatusMap.put(itemType, CrawlStatus.COMPLETED);
    }

    public void failCrawling(ItemType itemType) {
        crawlingStatusMap.put(itemType, CrawlStatus.FAILED);
    }

    public void removeCrawling(ItemType itemType)  {
        crawlingStatusMap.remove(itemType);
    }

    public boolean isRunning(ItemType itemType) {
        return crawlingStatusMap.get(itemType) == CrawlStatus.RUNNING;
    }
}
