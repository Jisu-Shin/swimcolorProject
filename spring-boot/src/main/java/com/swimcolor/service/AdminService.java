package com.swimcolor.service;

import com.swimcolor.client.FastapiClient;
import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final FastapiClient fastapiClient;
    private final SwimsuitService swimsuitService;

    public void crawl(String url) {
        CrawlResponseDto crawlResponseDto = fastapiClient.crawlProducts(url);
        int count = swimsuitService.saveSwimsuit(crawlResponseDto);
        log.debug("#### 저장된 row {} ", count);
    }

}
