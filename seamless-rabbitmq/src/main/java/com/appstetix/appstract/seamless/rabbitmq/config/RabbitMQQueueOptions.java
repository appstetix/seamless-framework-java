package com.appstetix.appstract.seamless.rabbitmq.config;

public interface RabbitMQQueueOptions {

    Boolean autoAck();
    Boolean keepMostRecent();
    Integer maxInternalQueueSize();

}
