package com.swimcolor.dto;

import lombok.Data;

@Data
public class SimilarListDto {
    private String suitHexColor;
    private String swimcapId;
    private String capHexColor;
    private Double similarity;
}
