package com.swimcolor.exception;

public class ColorMatchException extends BusinessException {
    public ColorMatchException(ErrorCode errorCode) {
        super(errorCode);
    }
}
