package com.swimcolor.repository;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.repository.dto.ColorRecommendDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaSwimcapRepository extends JpaRepository<Swimcap, String> {
    @Query("SELECT s FROM Swimcap s LEFT JOIN FETCH s.colors WHERE s.id IN :ids")
    List<Swimcap> findByIdsWithColors(@Param("ids") List<String> ids);

    @Query("""
        SELECT s
        FROM Swimcap s
        LEFT JOIN FETCH s.colors c
        LEFT JOIN ColorMatch cm 
               ON s.id = cm.swimcapId
               AND cm.swimsuitId = :swimsuitId
        WHERE s.id IN :ids
        AND cm.algorithmVersion = :algorithmVersion
        ORDER BY cm.similarityScore
        """)
    List<Swimcap> findByIdsWithColors(
            @Param("ids") List<String> ids,
            @Param("swimsuitId") String swimsuitId,
            @Param("algorithmVersion") String algorithmVersion
    );

    @Query(value = """
            select 
                    scp.swimcap_id as swimcapId,  
                    scp.colors as color
            from swimcap_palette scp;
            """, nativeQuery = true)
    List<ColorRecommendDto> findAllColors();
}
