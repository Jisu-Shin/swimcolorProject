package com.swimcolor.mapper;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.dto.RecommendListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ColorMatchMapper {

    ColorMatch toEntity(RecommendListDto dto);
}

