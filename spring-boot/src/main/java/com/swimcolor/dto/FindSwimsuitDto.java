package com.swimcolor.dto;

import lombok.Data;

import java.util.List;

@Data
public class FindSwimsuitDto {
    List<String> brands;
    List<SwimsuitListDto> swimsuitList;
}
