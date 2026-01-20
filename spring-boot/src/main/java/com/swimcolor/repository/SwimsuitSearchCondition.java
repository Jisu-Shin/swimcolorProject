package com.swimcolor.repository;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class SwimsuitSearchCondition {
    private List<String> brands;

    // todo 컨트롤러에서 할지 고민해보자
    public void setBrandsFromParam(String brandsParam) {
        if (brandsParam == null || brandsParam.isBlank()) {
            this.brands = null;
            return;
        }

        this.brands = Arrays.stream(brandsParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
