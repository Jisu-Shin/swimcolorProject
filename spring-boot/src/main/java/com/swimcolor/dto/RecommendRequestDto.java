package com.swimcolor.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendRequestDto {
    private String swimsuitId;
    private List<String> colors;
}
