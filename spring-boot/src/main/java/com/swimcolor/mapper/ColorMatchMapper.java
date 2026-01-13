package com.swimcolor.mapper;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.dto.ColorMatchListDto;
import com.swimcolor.dto.ColorMatchViewDto;
import com.swimcolor.dto.RecommendListDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ColorMatchMapper {
    ColorMatch toEntity(RecommendListDto dto);
    ColorMatchListDto toDto(ColorMatchViewDto viewDto);
}

