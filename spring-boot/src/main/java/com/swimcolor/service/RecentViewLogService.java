package com.swimcolor.service;

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
    public void save(String viewId, ViewType viewType) {
        RecentViewLog recentViewLog = RecentViewLog.builder()
                .viewId(viewId)
                .viewType(viewType)
                .lastViewedAt(LocalDateTime.now())
                .build();
        jpaRecentViewLogRepository.save(recentViewLog);
    }

    /**
     * viewId (swimsuit id) 를 받아 수모를 크롤링한 날짜랑 비교한다.
     * 수모를 크롤링한 날짜 < viewId를 조회한 날짜 경우, 조회 시점보다 과거에 크롤링 한 것이다
     * @param viewId
     * @return
     */
    public boolean isAfterCrawling(String viewId) {
        Integer dateDiff = jpaRecentViewLogRepository.getDateDiff(viewId);
        log.info("크롤링 날짜 비교(0,음수일경우 DB조회 / 양수일경우 외부호출 필요) : {} ", dateDiff);

        return dateDiff > 0 ? true : false;
    }
}
