package com.swimcolor.mapper;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlListDto;
import com.swimcolor.dto.RecommendListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ColorMatchMapper {
    @Mapping(source = "swimsuitId", target = "swimsuitId")
    @Mapping(source = "swimcapId", target = "swimcapId")
    @Mapping(source = "suitHexColor", target = "suitHexColor")
    @Mapping(source = "capHexColor", target = "capHexColor")
    @Mapping(source = "similarityScore", target = "similarityScore")
    ColorMatch toEntity(RecommendListDto dto);
}

