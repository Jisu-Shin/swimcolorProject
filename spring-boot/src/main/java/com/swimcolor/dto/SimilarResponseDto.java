package com.swimcolor.dto;

import lombok.Data;

import java.util.List;

@Data
public class SimilarResponseDto {
    private String swimsuitId;
    private List<SimilarListDto> similarList;
}
