package com.swimcolor.mapper;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlListDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwimsuitMapperTest {

    private final SwimsuitMapper swimsuitMapper = Mappers.getMapper(SwimsuitMapper.class);

    @Test
    public void 엔티티변경() throws Exception {
        //given
        CrawlListDto crawlListDto = new CrawlListDto();
        crawlListDto.setBrand("abc");
        crawlListDto.setName("수영복이름");
        crawlListDto.setPrice(15000);
        crawlListDto.setColors(List.of("#456789, #eadb12"));

        //when
        Swimsuit entity = swimsuitMapper.toEntity(crawlListDto, 5L);

        //then
        System.out.println("entity = " + entity);
    }

}