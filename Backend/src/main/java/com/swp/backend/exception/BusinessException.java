package com.swp.backend.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode.getMessage(), args));
        this.errorCode = errorCode;
        this.args = args;
    }

    private static String formatMessage(String message, Object... args) {
        return (args == null || args.length == 0) ? message : String.format(message, args);
    }
}
