package com.swimcolor.controller.api;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import com.swimcolor.service.CrawlStatusService;
import com.swimcolor.service.CrawlingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingApiController {

    private final CrawlStatusService crawlStatusService;
    private final CrawlingLogService crawlingLogService;

    @GetMapping("/status/{category}")
    public ResponseEntity<String> getCrawlStatus(@PathVariable String category) {
        String status = crawlStatusService.getCrawlStatus(category);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/status/{category}")
    public ResponseEntity<Void> removeCrawlStatus(@PathVariable String category) {

        CrawlingLog lastLog;
        if (ItemType.SWIMCAP.name().equals(category)) {
            crawlStatusService.removeSwimcapCrawling();
            lastLog = crawlingLogService.getLastSwimcapCrawlingLog(ItemType.SWIMCAP);
        } else {
            // SWIMSUIT
            crawlStatusService.removeSwimsuitCrawling();
            lastLog = crawlingLogService.getLastSwimcapCrawlingLog(ItemType.SWIMSUIT);
        }

        crawlingLogService.updateCrawlingLog(lastLog.getId(), CrawlStatus.FAILED, 0, "ADMIN REQUEST FAILED");

        return ResponseEntity.ok().build();
    }
}
