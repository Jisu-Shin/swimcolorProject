package com.swimcolor.repository;

import com.swimcolor.domain.ColorMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaColorMatchRepository extends JpaRepository<ColorMatch, Long> {
    List<ColorMatch> findBySwimsuitIdOrderBySimilarityDesc(String swimsuitId);
}
