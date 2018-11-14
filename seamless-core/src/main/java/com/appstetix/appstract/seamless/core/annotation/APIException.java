package com.appstetix.appstract.seamless.core.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface APIException {
    Class<? extends Throwable> value();
}
