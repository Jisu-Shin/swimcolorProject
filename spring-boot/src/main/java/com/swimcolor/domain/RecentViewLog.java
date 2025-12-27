package com.swimcolor.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentViewLog {

    @Id
    @Column(nullable = false)
    private String viewId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ViewType viewType;

    @Column(nullable = false)
    private LocalDateTime lastViewedAt;

    @Builder
    private RecentViewLog(String viewId, ViewType viewType, LocalDateTime lastViewedAt) {
        this.viewId = viewId;
        this.viewType = viewType;
        this.lastViewedAt = lastViewedAt;
    }

    public void updateLastViewedAt() {
        this.lastViewedAt = LocalDateTime.now();
    }
}
