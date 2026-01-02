package com.swimcolor.controller.api;

import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingApiController {

    private final AdminService adminService;

    @PostMapping("/callback/swimsuits")
    public ResponseEntity<Void> getSwimsuitCrawlingResult(@RequestBody CrawlResponseDto crawlResponseDto) {
        adminService.responseCrawlSwimsuits(crawlResponseDto);
        return ResponseEntity.ok().build();
    }
}
