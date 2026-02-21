package com.swimcolor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendListDto {
    private String swimsuitId;
    private String suitHexColor;
    private String swimcapId;
    private String capHexColor;
    private Double similarityScore;
    private String algorithmVersion;
}
