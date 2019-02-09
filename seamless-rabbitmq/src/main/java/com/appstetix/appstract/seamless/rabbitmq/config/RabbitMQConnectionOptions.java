package com.appstetix.appstract.seamless.rabbitmq.config;

import io.vertx.core.json.JsonObject;

import java.util.Map;

public interface RabbitMQConnectionOptions {

    Map<String, Object> options();

}
