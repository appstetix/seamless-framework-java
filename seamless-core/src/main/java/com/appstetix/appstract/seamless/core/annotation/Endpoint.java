package com.appstetix.appstract.seamless.core.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint {

    String path() default "";
    int version() default 0;
    HttpMethod method() default HttpMethod.GET;
    boolean secure() default true;

}
