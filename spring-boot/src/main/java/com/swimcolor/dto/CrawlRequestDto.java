package com.swimcolor.dto;

import lombok.Data;

@Data
public class CrawlRequestDto {
    private Long logId;
    private String crawlingUrl;
    private String callbackUrl;
}
