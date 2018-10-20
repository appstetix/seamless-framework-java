package com.appstetix.appstract.seamless.core.exception;

public class MissingHandlerException extends RuntimeException {
    public MissingHandlerException() {
    }

    public MissingHandlerException(String message) {
        super(message);
    }

    public MissingHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingHandlerException(Throwable cause) {
        super(cause);
    }

    public MissingHandlerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
