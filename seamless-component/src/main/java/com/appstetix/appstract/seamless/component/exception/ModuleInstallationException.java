package com.appstetix.appstract.seamless.component.exception;

public class ModuleInstallationException extends RuntimeException {

    public ModuleInstallationException() {
    }

    public ModuleInstallationException(String message) {
        super(message);
    }

    public ModuleInstallationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleInstallationException(Throwable cause) {
        super(cause);
    }

    public ModuleInstallationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
