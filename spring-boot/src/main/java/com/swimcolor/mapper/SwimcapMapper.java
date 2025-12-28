package com.swimcolor.mapper;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.CrawlListDto;
import com.swimcolor.dto.SwimcapListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SwimcapMapper {
    SwimcapListDto toDto(Swimcap swimcap);

    @Mapping(source = "img_url", target = "imageUrl")
    @Mapping(source = "product_url", target = "productUrl")
    @Mapping(source = "colors", target = "colors")
    Swimcap toEntity(CrawlListDto dto);
}

