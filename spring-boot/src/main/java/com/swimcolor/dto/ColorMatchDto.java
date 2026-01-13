package com.swimcolor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ColorMatchDto {
    String swimsuitId;
    String swimsuitName;
    String swimsuitImageUrl;
    List<ColorMatchListDto> colorMathDetailList;
}
