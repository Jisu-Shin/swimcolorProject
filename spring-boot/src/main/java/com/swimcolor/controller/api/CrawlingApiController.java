package com.swimcolor.controller.api;

import com.swimcolor.domain.ItemType;
import com.swimcolor.service.CrawlStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingApiController {

    private final CrawlStatusService crawlStatusService;

    @GetMapping("/status/{category}")
    public ResponseEntity<String> getCrawlStatus(@PathVariable String category) {
        String status = crawlStatusService.getCrawlStatus(category);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/status/{category}")
    public ResponseEntity<Void> removeCrawlStatus(@PathVariable String category) {
        if (ItemType.SWIMCAP.name().equals(category)) {
            crawlStatusService.removeSwimcapCrawling();
        }

        if(ItemType.SWIMSUIT.name().equals(category)){
            crawlStatusService.removeSwimsuitCrawling();
        }

        return ResponseEntity.ok().build();
    }
}
