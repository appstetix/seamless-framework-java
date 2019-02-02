package com.appstetix.appstract.seamless.core.annotation;

import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionContainer;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;
import com.appstetix.appstract.seamless.core.exception.handler.DefaultExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableExceptionHandling {
    Class<? extends ExceptionHandler>[] exceptions() default {};
    Class<? extends ExceptionContainer> exceptionsClass() default DefaultExceptionsClass.class;
    Class<? extends ExceptionHandler> defaultHandler() default DefaultExceptionHandler.class;

    class DefaultExceptionsClass implements ExceptionContainer {
        @Override
        public Class<? extends ExceptionHandler>[] getHandlerClasses() {
            return null;
        }
    }

}
