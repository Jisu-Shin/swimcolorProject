package com.swimcolor.dto;

import com.swimcolor.domain.CrawlStatus;
import lombok.Data;
import java.util.List;

@Data
public class CrawlResponseDto {
    private Long logId;
    private CrawlStatus crawlStatus;
    private String errorMsg;
    private List<CrawlListDto> products;
}
