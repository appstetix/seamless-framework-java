package com.appstetix.appstract.seamless.core.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Tasks.class)
public @interface Task {
    String value();
    boolean secure() default true;
}
