package com.swimcolor.exception;

public class CrawlingException extends BusinessException {
    public CrawlingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
