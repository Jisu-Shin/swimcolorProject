package com.swimcolor.repository;

import com.swimcolor.domain.RecentViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRecentViewLogRepository extends JpaRepository<RecentViewLog, Long> {
    @Query(value = """
        SELECT TIMESTAMPDIFF(MINUTE,
            (SELECT IFNULL(MAX(last_viewed_at), NOW())
             FROM recent_view_log
             WHERE view_type = 'CRAWL_SWIMCAP'
             LIMIT 1),
            (SELECT IFNULL(MAX(last_viewed_at), NOW())
             FROM recent_view_log
             WHERE view_id = :viewId)
        )
        """, nativeQuery = true)
    Integer getMinuteDiff(@Param("viewId") String viewId);
}
