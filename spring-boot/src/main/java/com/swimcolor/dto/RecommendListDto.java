package com.swimcolor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecommendListDto {
    private String swimsuitId;
    private String suitHexColor;
    private String swimcapId;
    private String capHexColor;
    private Double similarityScore;
    private String algorithmVersion;
}
