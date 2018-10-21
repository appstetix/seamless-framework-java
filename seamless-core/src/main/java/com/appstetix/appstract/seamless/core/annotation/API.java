package com.appstetix.appstract.seamless.core.annotation;

import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.exception.APIViolationException;
import com.appstetix.appstract.seamless.core.exception.SeamlessFilterException;
import com.appstetix.appstract.seamless.core.generic.APIFilter;
import com.appstetix.appstract.seamless.core.generic.APIValidator;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
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
    Class<? extends APIFilter>[] filters() default DEFAULT_FILTER.class;
    long requestTimeout() default 0L;

    interface DEFAULT_VALIDATOR extends APIValidator { }
    interface DEFAULT_FILTER extends APIFilter { }
    interface DEFAULT_MESSAGE_CODEC extends MessageCodec { }
}
