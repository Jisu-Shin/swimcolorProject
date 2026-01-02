package com.swimcolor.controller.api;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.service.AdminService;
import com.swimcolor.service.CrawlStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminService adminService;
    private final CrawlStatusService crawlStatusService;

    @PostMapping("/crawlSwimsuits")
    public ResponseEntity<String> crawlSwimsuits(@RequestBody CrawlRequestDto requestDto) {
        adminService.crawlSwimsuits(requestDto.getCrawlingUrl());
        return ResponseEntity.ok("작업 시작됨");
    }

    @PostMapping("/crawlSwimcaps")
    public ResponseEntity<String> cralSwimcaps(@RequestBody CrawlRequestDto requestDto) {
        adminService.crawlSwimcaps(requestDto.getCrawlingUrl());
        return ResponseEntity.ok("작업 시작됨");
    }

    @GetMapping("/crawlStatus/{category}")
    public ResponseEntity<String> getCrawlStatus(@PathVariable String category) {
        String status = crawlStatusService.getCrawlStatus(category);
        return ResponseEntity.ok(status);
    }
}
