package com.swimcolor.dto;

import lombok.Data;

@Data
public class RecommendListDto {
    private String swimsuitId;
    private String suitHexColor;
    private String swimcapId;
    private String capHexColor;
    private Double similarityScore;
}
