package com.swimcolor.controller.api;

import com.swimcolor.dto.CrawlRequestDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminService adminService;

    @PostMapping("/crawlSwimsuits")
    public ResponseEntity<String> crawlSwimsuits(@Valid @RequestBody CrawlRequestDto requestDto) {
        adminService.crawlSwimsuits(requestDto.getCrawlingUrl());

        URI location = URI.create("/admin");
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(location)
                .build();
    }

    @PostMapping("/crawlSwimcaps")
    public ResponseEntity<String> cralSwimcaps(@Valid @RequestBody CrawlRequestDto requestDto) {
        adminService.crawlSwimcaps(requestDto.getCrawlingUrl());

        URI location = URI.create("/admin");
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(location)
                .build();
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
