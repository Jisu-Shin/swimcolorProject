package com.swimcolor.dto;

import lombok.Data;
import java.util.List;

@Data
public class CrawlListDto {
    private String brand;
    private String name;
    private int price;
    private String product_url;
    private String img_url;
    private boolean is_sold_out;
    private List<String> colors;
}
