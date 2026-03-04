package com.swimcolor.controller.api;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import com.swimcolor.service.CrawlingStateManager;
import com.swimcolor.service.CrawlingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingApiController {

    private final CrawlingStateManager crawlingStateManager;
    private final CrawlingLogService crawlingLogService;

    @GetMapping("/status/{itemType}")
    public ResponseEntity<Boolean> getCrawlStatus(@PathVariable String itemType) {
        boolean status = crawlingStateManager.isRunning(ItemType.valueOf(itemType));
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/status/{itemType}")
    public ResponseEntity<Void> removeCrawlStatus(@PathVariable String itemType) {

        CrawlingLog lastLog;
        if (ItemType.SWIMCAP.name().equals(itemType)) {
            crawlingStateManager.removeCrawling(ItemType.SWIMCAP);
            lastLog = crawlingLogService.getLastSwimcapCrawlingLog(ItemType.SWIMCAP);

        } else {
            crawlingStateManager.removeCrawling(ItemType.SWIMSUIT);
            lastLog = crawlingLogService.getLastSwimcapCrawlingLog(ItemType.SWIMSUIT);
        }

        crawlingLogService.updateCrawlingLog(lastLog.getId(), CrawlStatus.FAILED, 0, "ADMIN REQUEST FAILED");

        return ResponseEntity.noContent().build();
    }
}
