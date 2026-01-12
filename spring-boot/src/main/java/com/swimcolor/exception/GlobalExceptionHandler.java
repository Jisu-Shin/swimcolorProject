package com.swimcolor.exception;

import com.swimcolor.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * - REST API의 모든 예외를 일관되게 처리
 * - 상세한 로깅으로 디버깅 및 모니터링 지원
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리 (프로젝트의 모든 비즈니스 예외)
     * - CrawlingException 등 포함
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[BusinessException] Code: {}, Message: {}, Path: {}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI());

        ErrorResponseDto response = ErrorResponseDto.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getStatus().value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(response);
    }

    /**
     * Validation 예외 처리
     * - @Valid, @Validated 검증 실패 시
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        // 검증 실패한 필드들 로깅
        String errors = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.joining(", "));

        log.warn("[ValidationException] Path: {}, Errors: {}",
                request.getRequestURI(), errors);

        ErrorResponseDto response = ErrorResponseDto.builder()
                .code("CMN-001")
                .message("입력값이 올바르지 않습니다: " + errors)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /**
     * 그 외 모든 예외 처리 (최종 방어선)
     * - 예상하지 못한 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(
            Exception e,
            HttpServletRequest request) {

        log.error("[UnexpectedException] Path: {}, Type: {}, Message: {}",
                request.getRequestURI(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                e); // 스택트레이스 출력

        ErrorResponseDto response = ErrorResponseDto.builder()
                .code("CMN-500")
                .message("서버 내부 오류가 발생했습니다")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
