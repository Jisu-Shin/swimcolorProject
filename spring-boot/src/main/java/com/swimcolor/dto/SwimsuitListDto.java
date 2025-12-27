package com.swimcolor.dto;

import lombok.Data;

@Data
public class SwimsuitListDto {
    private String name;
    private String imageUrl;
    private String productUrl;
    private String brand;
    private Integer price;
    // todo : colors 안들어감
}
