package com.swimcolor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColorMatchListDto {
    String suitHexColor;
    String swimcapId;
    String swimcapName;
    String swimcapImageUrl;
    String capHexColor;
    Double similarityScore;
    Long colorMatchId;
}
