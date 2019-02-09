package com.appstetix.appstract.seamless.rabbitmq.annotation;

import com.appstetix.appstract.seamless.rabbitmq.config.RabbitMQConnectionOptions;

import java.lang.annotation.*;
import java.util.Map;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueueConnection {

    String DEFAULT_VALUE = "null";

    String id();
    String host() default DEFAULT_VALUE;
    int port() default -1;
    String user() default DEFAULT_VALUE;
    String password() default DEFAULT_VALUE;
    String vHost() default "/";

    Class<? extends RabbitMQConnectionOptions> connectionClass() default DefaultConnectionOptions.class;

    public class DefaultConnectionOptions implements RabbitMQConnectionOptions {
        @Override
        public Map<String, Object> options() {
            return null;
        }
    }
}
