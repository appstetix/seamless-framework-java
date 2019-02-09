package com.appstetix.appstract.seamless.core.exception.setup;

public class DynamicException extends RuntimeException {
    public DynamicException() {
    }

    public DynamicException(String message) {
        super(message);
    }

    public DynamicException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicException(Throwable cause) {
        super(cause);
    }

    public DynamicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
