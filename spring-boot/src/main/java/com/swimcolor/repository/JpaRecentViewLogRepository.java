package com.swimcolor.repository;

import com.swimcolor.domain.RecentViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRecentViewLogRepository extends JpaRepository<RecentViewLog, Long> {
}
