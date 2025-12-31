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
public class CrawlingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceUrl; // 크롤링한 URL

    @Column(nullable = false)
    private LocalDateTime crawledAt; // 크롤링 수행 일시

    private int totalCount; // 크롤링한 상품 개수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType; // 수영복(SWIMSUIT)인지 수모(SWIMCAP)인지 구분

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CrawlingStatus status; // SUCCESS, FAIL

    @Column(columnDefinition = "TEXT") // 에러 메시지가 길 수 있으므로 TEXT 타입 권장
    private String errorMessage; // 실패 시 에러 내용

    private Long executionTime; // 크롤링 소요 시간 (ms)

    @Builder
    public CrawlingLog(Long id, String sourceUrl, LocalDateTime crawledAt, int totalCount, ItemType itemType, CrawlingStatus status, String errorMessage, Long executionTime) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.crawledAt = crawledAt;
        this.totalCount = totalCount;
        this.itemType = itemType;
        this.status = status;
        this.errorMessage = errorMessage;
        this.executionTime = executionTime;
    }
}
