package com.appstetix.appstract.seamless.rabbitmq.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExchangeListeners {
    ListenToExchange[] value();
}
