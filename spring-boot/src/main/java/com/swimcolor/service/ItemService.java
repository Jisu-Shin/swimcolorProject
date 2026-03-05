package com.swimcolor.service;

import com.swimcolor.domain.ItemType;
import com.swimcolor.dto.CrawlResponseDto;

public interface ItemService {
    int save(CrawlResponseDto responseDto);
    ItemType getItemType();
}
