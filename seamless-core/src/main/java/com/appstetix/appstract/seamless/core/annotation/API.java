package com.appstetix.appstract.seamless.core.annotation;

import com.appstetix.appstract.seamless.core.validator.APIValidator;
import io.vertx.core.AbstractVerticle;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface API {

    Class<? extends AbstractVerticle>[] handlers() default {};
    Class<? extends APIValidator>[] validators() default {};
    long requestTimeout() default 0L;

}
