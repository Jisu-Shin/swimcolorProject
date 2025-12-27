package com.swimcolor.mapper;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.SwimcapListDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SwimcapMapper {
    SwimcapListDto toDto(Swimcap swimcap);
}

