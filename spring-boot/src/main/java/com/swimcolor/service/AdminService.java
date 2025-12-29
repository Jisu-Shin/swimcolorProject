package com.swimcolor.service;

import com.swimcolor.client.FastapiClient;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendResponseDto;
import com.swimcolor.mapper.ColorMatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final FastapiClient fastapiClient;
    private final SwimsuitService swimsuitService;
    private final SwimcapService swimcapService;
    private final ColorMatchService colorMatchService;

    public void crawlSwimsuits(String url) {
        CrawlResponseDto crawlResponseDto = fastapiClient.crawlSwimsuits(url);
        int count = swimsuitService.saveSwimsuit(crawlResponseDto);
        log.debug("#### 저장된 row {} ", count);
    }

    public void crawlSwimcaps(String url) {
        CrawlResponseDto crawlResponseDto = fastapiClient.crawlSwimcaps(url);
        int count = swimcapService.saveSwimcap(crawlResponseDto);
        log.debug("#### 저장된 row {} ", count);
    }

    public void recommendSwimcaps(String swimsuitId, List<String> colors) {
        RecommendResponseDto recommendSwimcap = fastapiClient.getRecommendSwimcap(swimsuitId, colors);
        colorMatchService.saveColorMatch(recommendSwimcap);
    }

}
