package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaSwimsuitRepository extends JpaRepository<Swimsuit, String>, SwimsuitQueryDsl {
    @Query("SELECT DISTINCT s.brand FROM Swimsuit s")
    List<String> findDistinctAllBrands();
}
