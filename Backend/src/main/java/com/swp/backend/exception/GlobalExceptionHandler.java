package com.swp.backend.exception;

import com.swp.backend.dto.common.ResponseObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseObject> handleBusinessException(BusinessException ex) {

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(
                new ResponseObject(
                        ex.getErrorCode().getStatus().value(),
                        ex.getMessage(),
                        null
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject> handleUnexpectedException(Exception ex) {

        log.error("Unexpected error: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject(
                        500,
                        "Lỗi hệ thống: " + ex.getMessage(),
                        null
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseObject> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ResponseObject(
                        400,
                        "Validation falied",
                        errors
                )
        );
    }
}