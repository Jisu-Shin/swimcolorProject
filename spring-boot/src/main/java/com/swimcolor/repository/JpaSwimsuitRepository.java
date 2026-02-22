package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaSwimsuitRepository extends JpaRepository<Swimsuit, String>, SwimsuitQueryDsl {
    @Query("SELECT DISTINCT s.brand FROM Swimsuit s")
    List<String> findDistinctAllBrands();

    @Query(value = """

            select
                    s.id                ,
                    s.brand             ,
                    s.crawling_log_id   ,
                    s.image_url         ,
                    s.name              ,
                    s.price             ,
                    s.product_url      
            from swimsuit s
            join (
                select swimsuit_id
                from color_match
                group by swimsuit_id
                order by count(*) desc
                limit 10
            ) t on s.id = t.swimsuit_id;
            """, nativeQuery = true)
    List<Swimsuit> findPopularSwimsuits();
}
