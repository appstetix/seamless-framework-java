package com.appstetix.appstract.seamless.core.exception;

public class SeamlessFilterException extends RuntimeException {
    public SeamlessFilterException() {
    }

    public SeamlessFilterException(String message) {
        super(message);
    }

    public SeamlessFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeamlessFilterException(Throwable cause) {
        super(cause);
    }

    public SeamlessFilterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
