package com.swimcolor.mapper;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlListDto;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.SwimsuitListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SwimsuitMapper {

    @Named("dtoToEntity")
    @Mapping(source = "img_url", target = "imageUrl")
    @Mapping(source = "product_url", target = "productUrl")
    @Mapping(source = "brand", target = "brand")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "colors", target = "colors")
    Swimsuit toEntity(CrawlListDto dto);

    default List<Swimsuit> toEntity(CrawlResponseDto dto) {
        if (dto == null || dto.getProducts() == null) return null;
        return dto.getProducts().stream()
                .map(this::toEntity)
                .toList();
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "colors", target = "colors")
    SwimsuitListDto toDto(Swimsuit swimsuit);
}

