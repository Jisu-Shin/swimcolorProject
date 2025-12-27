package com.swimcolor.mapper;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlListDto;
import com.swimcolor.dto.SwimsuitListDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-27T05:37:24+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Oracle Corporation)"
)
@Component
public class SwimsuitMapperImpl implements SwimsuitMapper {

    @Override
    public Swimsuit toEntity(CrawlListDto dto) {
        if ( dto == null ) {
            return null;
        }

        Swimsuit.SwimsuitBuilder swimsuit = Swimsuit.builder();

        swimsuit.imageUrl( dto.getImg_url() );
        swimsuit.productUrl( dto.getProduct_url() );
        swimsuit.brand( dto.getBrand() );
        swimsuit.name( dto.getName() );
        swimsuit.price( dto.getPrice() );
        List<String> list = dto.getColors();
        if ( list != null ) {
            swimsuit.colors( new ArrayList<String>( list ) );
        }

        return swimsuit.build();
    }

    @Override
    public SwimsuitListDto toDto(Swimsuit swimsuit) {
        if ( swimsuit == null ) {
            return null;
        }

        SwimsuitListDto swimsuitListDto = new SwimsuitListDto();

        swimsuitListDto.setName( swimsuit.getName() );
        swimsuitListDto.setImageUrl( swimsuit.getImageUrl() );
        swimsuitListDto.setProductUrl( swimsuit.getProductUrl() );
        swimsuitListDto.setBrand( swimsuit.getBrand() );
        swimsuitListDto.setPrice( swimsuit.getPrice() );

        return swimsuitListDto;
    }
}
