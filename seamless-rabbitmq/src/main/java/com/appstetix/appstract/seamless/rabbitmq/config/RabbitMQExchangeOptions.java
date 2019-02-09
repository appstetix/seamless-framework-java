package com.appstetix.appstract.seamless.rabbitmq.config;

public interface RabbitMQExchangeOptions {

    Boolean durable();
    Boolean exclusive();
    Boolean autoDelete();

}
