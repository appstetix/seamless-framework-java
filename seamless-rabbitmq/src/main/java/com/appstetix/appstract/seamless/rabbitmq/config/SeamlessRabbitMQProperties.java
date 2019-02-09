package com.appstetix.appstract.seamless.rabbitmq.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.appstetix.appstract.seamless.component.EnvVarUtil.IntegerValue;
import static com.appstetix.appstract.seamless.component.EnvVarUtil.StringValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeamlessRabbitMQProperties {

    public static final String HOST = StringValue("seamless.rabbitmq.host", "null");
    public static final int PORT = IntegerValue("seamless.rabbitmq.port", "0");
    public static final String USER = StringValue("seamless.rabbitmq.user", "null");
    public static final String PASSWORD = StringValue("seamless.rabbitmq.password", "null");
    public static final String VIRTUAL_HOST = StringValue("seamless.rabbitmq.vhost", "null");

}
