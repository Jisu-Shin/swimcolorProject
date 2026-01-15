package com.swimcolor.dto;

import com.swimcolor.domain.FeedbackType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ColorMatchFeedbackDto {
    private Long id;

    @NotNull(message = "색상매칭 ID는 필수값입니다.")
    private Long colorMatchId;

    private FeedbackType feedbackType;

    @NotEmpty(message = "알고리즘 버전은 필수값입니다.")
    private String algorithmVersion;

    @NotEmpty(message = "리뷰작성자는 필수값입니다.")
    private String reviewedBy;

    private String comment;
    private LocalDateTime reviewedAt;
}
