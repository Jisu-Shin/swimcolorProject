package com.swimcolor.service;

import com.swimcolor.domain.ItemType;
import com.swimcolor.domain.RecentViewLog;
import com.swimcolor.domain.ViewType;
import com.swimcolor.repository.JpaRecentViewLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentViewLogService {

    private final JpaRecentViewLogRepository jpaRecentViewLogRepository;

    @Transactional
    public void saveCrawlingLog(ItemType itemType) {
        ViewType viewType = itemType == ItemType.SWIMSUIT ? ViewType.CRAWL_SWIMSUIT : ViewType.CRAWL_SWIMCAP;
        RecentViewLog recentViewLog = RecentViewLog.builder()
                .viewId(itemType.name().concat("_LOG_ID"))
                .viewType(viewType)
                .lastViewedAt(LocalDateTime.now())
                .build();
        jpaRecentViewLogRepository.save(recentViewLog);
    }

    @Transactional
    public void save(String viewId, ViewType viewType) {
        RecentViewLog recentViewLog = RecentViewLog.builder()
                .viewId(viewId)
                .viewType(viewType)
                .lastViewedAt(LocalDateTime.now())
                .build();
        jpaRecentViewLogRepository.save(recentViewLog);
    }

    /**
     * swimsuitId (swimsuit id) 를 받아 수모를 크롤링한 날짜랑 비교한다.
     * 수모를 크롤링한 날짜 < viewId를 조회한 날짜 경우, 조회 시점보다 과거에 크롤링 한 것이다
     * @param swimsuitId
     * @return
     */
    public boolean isAfterCrawling(String swimsuitId) {
        Integer dateDiff = jpaRecentViewLogRepository.getMinuteDiff(swimsuitId);
        log.info("크롤링 시간(분) 비교(음수일경우 외부호출 / 0,양수일경우 DB조회 필요) : {} 분 ", dateDiff);

        return dateDiff < 0 ? true : false;
    }
}
