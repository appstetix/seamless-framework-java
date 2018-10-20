package com.appstetix.appstract.seamless.core.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Task {
    String value();
    boolean secure() default true;
}
