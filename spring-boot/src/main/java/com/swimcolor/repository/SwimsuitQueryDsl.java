package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SwimsuitQueryDsl {
    public List<Swimsuit> findBySearch(String keywords);
    public List<String> findRelatedBrands(String keywords);
    public Page<Swimsuit> findSwimsuitsBySearchCondition(SwimsuitSearchCondition condition, Pageable pageable);
}
