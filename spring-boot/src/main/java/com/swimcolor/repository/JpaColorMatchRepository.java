package com.swimcolor.repository;

import com.swimcolor.domain.ColorMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaColorMatchRepository extends JpaRepository<ColorMatch, Long> {
    List<ColorMatch> findBySwimsuitIdOrderBySimilarityScoreDesc(String swimsuitId);

    @Modifying
    @Query("delete from ColorMatch c where c.swimsuitId = :swimsuitId")
    void deleteBulkBySwimsuitId(@Param("swimsuitId") String swimsuitId);
}
