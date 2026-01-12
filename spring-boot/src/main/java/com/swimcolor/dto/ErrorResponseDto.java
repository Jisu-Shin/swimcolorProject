package com.swimcolor.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponseDto {
    private String code;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;
}
