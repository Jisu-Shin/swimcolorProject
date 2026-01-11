package com.swimcolor.mapper;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlListDto;
import com.swimcolor.dto.SwimsuitListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SwimsuitMapper {

    @Mapping(source = "logId", target="crawlingLogId")
    @Mapping(source = "dto.img_url", target = "imageUrl")
    @Mapping(source = "dto.product_url", target = "productUrl")
    @Mapping(source = "dto.brand", target = "brand")
    @Mapping(source = "dto.name", target = "name")
    @Mapping(source = "dto.price", target = "price")
    @Mapping(source = "dto.colors", target = "colors")
    Swimsuit toEntity(CrawlListDto dto, Long logId);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "colors", target = "colors")
    SwimsuitListDto toDto(Swimsuit swimsuit);
}

