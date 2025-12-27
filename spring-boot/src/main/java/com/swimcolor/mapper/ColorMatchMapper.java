package com.swimcolor.mapper;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.dto.SimilarListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ColorMatchMapper {

    @Mapping(source = "swimsuitId", target = "swimsuitId")
    ColorMatch toEntity(SimilarListDto dto, String swimsuitId);
}

