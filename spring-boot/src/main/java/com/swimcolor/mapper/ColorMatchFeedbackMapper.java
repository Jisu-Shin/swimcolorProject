package com.swimcolor.mapper;

import com.swimcolor.domain.ColorMatchFeedback;
import com.swimcolor.dto.ColorMatchFeedbackDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ColorMatchFeedbackMapper {
    ColorMatchFeedback toEntity(ColorMatchFeedbackDto dto);
}

