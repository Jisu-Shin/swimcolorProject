package com.swimcolor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequestDto {
    private Long logId;
    private String crawlingUrl;
    private String callbackUrl;

    public CrawlRequestDto(Long logId, String crawlingUrl) {
        this.logId = logId;
        this.crawlingUrl = crawlingUrl;
    }
}
