package com.appstetix.appstract.seamless.core.exception;

public class APIFilterException extends RuntimeException {

    private static final int DEFAULT_CODE = 400;

    private int code;

    public APIFilterException() {
    }

    public APIFilterException(String message) {
        this(DEFAULT_CODE, message);
    }

    public APIFilterException(int code, String message) {
        super(message);
        this.code = code;
    }

    public APIFilterException(String message, Throwable cause) {
        this(DEFAULT_CODE, message, cause);
    }

    public APIFilterException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public APIFilterException(Throwable cause) {
        this(DEFAULT_CODE, cause);
    }

    public APIFilterException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public APIFilterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this(DEFAULT_CODE, message, cause, enableSuppression, writableStackTrace);
    }

    public APIFilterException(int code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
