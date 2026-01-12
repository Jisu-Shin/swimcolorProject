package com.swimcolor.controller.api;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminService adminService;

    @PostMapping("/crawlSwimsuits")
    public ResponseEntity<String> crawlSwimsuits(@Valid @RequestBody CrawlRequestDto requestDto) {
        adminService.crawlSwimsuits(requestDto.getCrawlingUrl());
        return ResponseEntity.ok("작업 시작됨");
    }

    @PostMapping("/crawlSwimcaps")
    public ResponseEntity<String> cralSwimcaps(@Valid @RequestBody CrawlRequestDto requestDto) {
        adminService.crawlSwimcaps(requestDto.getCrawlingUrl());
        return ResponseEntity.ok("작업 시작됨");
    }

    @PostMapping("/callback/swimsuits")
    public ResponseEntity<Void> getSwimsuitCrawlingResult(@RequestBody CrawlResponseDto crawlResponseDto) {
        adminService.responseCrawlSwimsuits(crawlResponseDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/callback/swimcaps")
    public ResponseEntity<Void> getSwimcapCrawlingResult(@RequestBody CrawlResponseDto crawlResponseDto) {
        adminService.responseCrawlSwimcaps(crawlResponseDto);
        return ResponseEntity.ok().build();
    }
}
