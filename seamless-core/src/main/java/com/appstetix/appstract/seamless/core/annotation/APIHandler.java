package com.appstetix.appstract.seamless.core.annotation;

import com.appstetix.appstract.seamless.core.generic.AccessType;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface APIHandler {

    String baseURL() default "";
    AccessType access() default AccessType.ALL;
    
}
