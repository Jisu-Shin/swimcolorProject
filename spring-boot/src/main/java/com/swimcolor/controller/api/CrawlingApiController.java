package com.swimcolor.controller.api;

import com.swimcolor.domain.ItemType;
import com.swimcolor.service.CrawlingService;
import com.swimcolor.service.CrawlingStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingApiController {

    private final CrawlingStateManager crawlingStateManager;
    private final CrawlingService crawlingService;

    @GetMapping("/status/{itemType}")
    public ResponseEntity<Boolean> getCrawlStatus(@PathVariable String itemType) {
        boolean status = crawlingStateManager.isRunning(ItemType.valueOf(itemType));
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/status/{itemType}")
    public ResponseEntity<Void> removeCrawlStatus(@PathVariable String itemType) {
        log.info("itemType 확인 : {}", ItemType.valueOf(itemType));
        log.info("itemType == SWIMSUIT 확인 : {}", ItemType.valueOf(itemType)==ItemType.SWIMSUIT);
        crawlingService.removeCrawlingStatus(ItemType.valueOf(itemType));
        return ResponseEntity.noContent().build();
    }
}
