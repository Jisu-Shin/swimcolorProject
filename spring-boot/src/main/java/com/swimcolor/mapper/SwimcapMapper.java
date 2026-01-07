package com.swimcolor.mapper;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.CrawlListDto;
import com.swimcolor.dto.SwimcapListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SwimcapMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "colors", target = "colors")
    SwimcapListDto toDto(Swimcap swimcap);

    @Mapping(source = "dto.img_url", target = "imageUrl")
    @Mapping(source = "dto.product_url", target = "productUrl")
    @Mapping(source = "dto.colors", target = "colors")
    @Mapping(source = "logId", target = "crawlingLogId")
    Swimcap toEntity(CrawlListDto dto, Long logId);
}

