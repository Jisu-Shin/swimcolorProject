package com.swimcolor.dto;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.ItemType;
import lombok.Data;

@Data
public class CrawlingLogResponseDto {
    private Long id;
    private String sourceUrl;
    private String crawledAt;
    private int totalCount;
    private ItemType itemType;
    private CrawlStatus status;
    private String errorMessage;
    private Long executionTime;
}
