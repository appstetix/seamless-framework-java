package com.appstetix.appstract.seamless.data.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Assets {
    Class primary();
    Class test() default DEFAULT.class;

    final class DEFAULT {}
}
