package com.swimcolor.dto;

import lombok.Data;
import java.util.List;

@Data
public class CrawlResponseDto {
    private List<CrawlListDto> products;
}
