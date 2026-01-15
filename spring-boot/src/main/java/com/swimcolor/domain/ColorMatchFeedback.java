package com.swimcolor.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ColorMatchFeedback {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long colorMatchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackType feedbackType;

    @Column(nullable = false)
    private Integer algorithmVersion;

    @Column(length = 10, nullable = false)
    private String reviewedBy;

    private String comment;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    @Builder
    public ColorMatchFeedback(Long id, Long colorMatchId, FeedbackType feedbackType, Integer algorithmVersion, String reviewedBy, String comment, LocalDateTime reviewedAt) {
        this.id = id;
        this.colorMatchId = colorMatchId;
        this.feedbackType = feedbackType;
        this.algorithmVersion = algorithmVersion;
        this.reviewedBy = reviewedBy;
        this.comment = comment;
        this.reviewedAt = reviewedAt;
    }
}
