package com.swimcolor.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendResponseDto {
    private String swimsuitId;
    private List<RecommendListDto> similarList;
}
