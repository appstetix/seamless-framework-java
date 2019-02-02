package com.appstetix.appstract.seamless.core.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tasks {
    Task[] value();
}
