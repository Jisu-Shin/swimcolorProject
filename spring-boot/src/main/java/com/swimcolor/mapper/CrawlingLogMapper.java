package com.swimcolor.mapper;

import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.dto.CrawlingLogResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CrawlingLogMapper {

    @Mapping(target = "crawledAt", source = "crawledAt", dateFormat = "yyyy/MM/dd HH:mm")
    CrawlingLogResponseDto toDto(CrawlingLog crawlingLog);
}
