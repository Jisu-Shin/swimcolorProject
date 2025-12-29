package com.swimcolor.dto;

import lombok.Data;

import java.util.List;

@Data
public class SwimcapListDto {
    private String id;
    private String name;
    private String imageUrl;
    private String productUrl;
    private String brand;
    private Integer price;
    private List<String> colors;
}
