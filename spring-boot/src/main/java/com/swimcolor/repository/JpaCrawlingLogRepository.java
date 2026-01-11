package com.swimcolor.repository;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaCrawlingLogRepository extends JpaRepository<CrawlingLog,Long> {
    // 관리자 페이지에서 최근 크롤링 기록부터 보기 위한 쿼리
    List<CrawlingLog> findAllByOrderByCrawledAtDesc();

    // 특정 상태(예: FAIL)만 조회하고 싶을 때
    List<CrawlingLog> findByStatus(CrawlStatus status);

    @Query("""
    select cl
    from CrawlingLog cl
    where cl.itemType = :itemType
      and cl.status = 'RUNNING'
    order by cl.id desc
    """)
    List<CrawlingLog> findLastLogByItemType(@Param(value = "itemType") ItemType itemType);
}
