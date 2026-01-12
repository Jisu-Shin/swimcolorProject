package com.swimcolor.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequestDto {
    private Long logId;

    @NotEmpty(message = "크롤링URL 값은 필수입니다.")
    private String crawlingUrl;

    private String callbackUrl;
}
