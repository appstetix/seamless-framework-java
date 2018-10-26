package com.appstetix.appstract.seamless.core.annotation;

import com.appstetix.appstract.seamless.core.validator.APIValidator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageCodec;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface API {

    Class<? extends AbstractVerticle>[] handlers() default {};
    Class<? extends APIValidator> validator() default DEFAULT_VALIDATOR.class;
    long requestTimeout() default 0L;

    interface DEFAULT_VALIDATOR extends APIValidator { }
    interface DEFAULT_MESSAGE_CODEC extends MessageCodec { }
}
