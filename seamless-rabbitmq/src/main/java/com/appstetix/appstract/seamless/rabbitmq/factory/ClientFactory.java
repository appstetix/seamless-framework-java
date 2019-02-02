package com.appstetix.appstract.seamless.rabbitmq.factory;

import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientFactory {

    public static RabbitMQClient create(Vertx vertx) {
        return RabbitMQClient.create(vertx);
    }

    public static RabbitMQClient create(Vertx vertx, String host, int port, String username, String password, String vHost) {
        RabbitMQOptions options = new RabbitMQOptions();
        options.setHost(host);
        options.setPort(port);
        options.setUser(username);
        options.setPassword(password);
        options.setVirtualHost(vHost);
        return create(vertx, options);
    }

    public static RabbitMQClient create(Vertx vertx, RabbitMQOptions options) {
        return RabbitMQClient.create(vertx, options);
    }

}
