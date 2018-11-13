package com.appstetix.appstract.seamless.core.annotation;

import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableExceptionHandling {
    Class<? extends ExceptionHandler>[] exceptions() default {};
}
