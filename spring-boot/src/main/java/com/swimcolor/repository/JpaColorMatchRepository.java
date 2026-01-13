package com.swimcolor.repository;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.dto.ColorMatchViewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(
            value = """
        select
            cm.swimsuit_id        as swimsuitId,
            ss.name               as swimsuitName,
            ss.image_url          as swimsuitImageUrl,
            cm.suit_hex_color     as suitHexColor,
            cm.swimcap_id         as swimcapId,
            sc.name               as swimcapName,
            sc.image_url          as swimcapImageUrl,
            cm.cap_hex_color      as capHexColor,
            cm.similarity_score   as similarityScore,
            cm.id                 as colorMatchId
        from color_match cm
        join swimsuit ss on cm.swimsuit_id = ss.id
        join swimcap sc on cm.swimcap_id = sc.id
        order by cm.swimsuit_id, cm.similarity_score
        """,
            countQuery = """
        select count(*)
        from color_match cm
        join swimsuit ss on cm.swimsuit_id = ss.id
        join swimcap sc on cm.swimcap_id = sc.id
        """,
            nativeQuery = true
    )
    Page<ColorMatchViewDto> findColorMatches(Pageable pageable);
}
