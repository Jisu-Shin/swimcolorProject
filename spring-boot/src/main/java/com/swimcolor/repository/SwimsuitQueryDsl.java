package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;

import java.util.List;

public interface SwimsuitQueryDsl {
    public List<Swimsuit> findBySearch(String keywords);
    public List<String> findRelatedBrands(String keywords);
}
