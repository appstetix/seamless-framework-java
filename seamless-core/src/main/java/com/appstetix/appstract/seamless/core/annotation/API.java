package com.appstetix.appstract.seamless.core.annotation;

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
    Class<? extends MessageCodec> codec() default DEFAULT_MESSAGE_CODEC.class;

    final class DEFAULT_VALIDATOR implements APIValidator {
        @Override
        public void validate(SeamlessRequest request) throws RuntimeException {
            //THIS CLASS ACTS AS A DEFAULT VALUE FOR THE VALIDATOR IF NONE IS SPECIFIED
        }
    }

    final class DEFAULT_FILTER extends APIFilter {
        @Override
        public boolean handle(SeamlessRequest request, Object rawInput) throws SeamlessFilterException {
            //THIS CLASS ACTS AS A DEFAULT VALUE FOR THE FILTER IF NONE IS SPECIFIED
            return true;
        }
    }

    interface DEFAULT_MESSAGE_CODEC extends MessageCodec { }
}
