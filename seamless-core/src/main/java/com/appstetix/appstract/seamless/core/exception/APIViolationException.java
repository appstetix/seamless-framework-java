package com.appstetix.appstract.seamless.core.exception;

public class APIViolationException extends RuntimeException {
    public APIViolationException() {
    }

    public APIViolationException(String message) {
        super(message);
    }

    public APIViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public APIViolationException(Throwable cause) {
        super(cause);
    }

    public APIViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
