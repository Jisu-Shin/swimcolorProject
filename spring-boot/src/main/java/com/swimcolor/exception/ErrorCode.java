package com.swimcolor.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Crawling Exceptions
    CRAWLING_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "CRAWL-001", "해당 카테고리의 크롤링이 이미 실행 중입니다."),
    FASTAPI_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "CRAWL-003", "외부 서버와 연결할 수 없습니다."),

    // ColorMatch Exceptions
    COLOR_MATCH_FEEDBACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "FEEDBACK-001", "이미 색상 매치 피드백을 등록하였습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
