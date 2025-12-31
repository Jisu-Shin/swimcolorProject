package com.swimcolor.mapper;

import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.dto.CrawlingLogResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CrawlingLogMapper {

    CrawlingLogResponseDto toDto(CrawlingLog crawlingLog);
}
