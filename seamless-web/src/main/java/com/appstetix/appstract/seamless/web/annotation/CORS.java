package com.appstetix.appstract.seamless.web.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CORS {

    String origins();
    HttpMethod[] methods() default {};
    String[] headers() default {};

}
