package com.appstetix.appstract.seamless.core.exception.custom;

public class ExceptionResolverException extends RuntimeException {
    public ExceptionResolverException() {
    }

    public ExceptionResolverException(String message) {
        super(message);
    }

    public ExceptionResolverException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionResolverException(Throwable cause) {
        super(cause);
    }

    public ExceptionResolverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
