package com.likelion.commit.gloabal.exception;


import com.likelion.commit.gloabal.response.ApiResponse;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.gloabal.response.status.BaseErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 유효성 검사를 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> failedValidations = new HashMap<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        fieldErrors.forEach(error -> failedValidations.put(error.getField(), error.getDefaultMessage()));
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.onFailure(
                ErrorCode.VALIDATION_FAILED.getCode(),
                ErrorCode.VALIDATION_FAILED.getMessage(),
                failedValidations);
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    // Custom 예외에 대한 처리
    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.warn("[WARNING] Custom Exception : {}", e.getErrorCode());
        BaseErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.
                status(errorCode.getHttpStatus())
                .body(errorCode.getErrorResponse());
    }

    // 그 외의 정의되지 않은 모든 예외 처리 (디버깅을 위해 일시 무효화)
//    @ExceptionHandler({Exception.class})
//    public ResponseEntity<ApiResponse<String>> handleAllException(Exception e) {
//        log.error("[WARNING] Internal Server Error : {} ", e.getMessage());
//        BaseErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR_500;
//        ApiResponse<String> errorResponse = ApiResponse.onFailure(
//                errorCode.getCode(),
//                errorCode.getMessage(),
//                e.getMessage()
//        );
//        return ResponseEntity
//                .status(errorCode.getHttpStatus())
//                .body(errorResponse);
//    }

}