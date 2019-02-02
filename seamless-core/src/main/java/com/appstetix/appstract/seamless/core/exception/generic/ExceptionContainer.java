package com.appstetix.appstract.seamless.core.exception.generic;

public interface ExceptionContainer {
    Class<? extends ExceptionHandler>[] getHandlerClasses();
}
