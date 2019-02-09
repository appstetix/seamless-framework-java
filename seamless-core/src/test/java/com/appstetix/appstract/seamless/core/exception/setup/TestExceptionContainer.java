package com.appstetix.appstract.seamless.core.exception.setup;

import com.appstetix.appstract.seamless.core.exception.generic.ExceptionContainer;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;

public class TestExceptionContainer implements ExceptionContainer {
    @Override
    public Class<? extends ExceptionHandler>[] getHandlerClasses() {
        return new Class[] {
                TestExceptionHandler.class
        };
    }

}
