package com.appstetix.appstract.seamless.rabbitmq.annotation;

import com.appstetix.appstract.seamless.rabbitmq.config.RabbitMQExchangeOptions;
import com.appstetix.appstract.seamless.rabbitmq.config.RabbitMQQueueOptions;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ExchangeListeners.class)
public @interface ListenToExchange {

    String DEFAULT_ROUTING_KEY = "#";
    String DEFAULT_QUEUE_NAME = "null";

    String exchangeName();
    String connectionId() default ListenToQueue.DEFAULT_CONNECTION;
    String queueName() default DEFAULT_QUEUE_NAME;
    String routingKey() default DEFAULT_ROUTING_KEY;

    Class<? extends RabbitMQExchangeOptions> exchangeOptions() default DefaultExchangeOptions.class;
    Class<? extends RabbitMQQueueOptions> queueOptionsClass() default ListenToQueue.DefaultRabbitMQQueueOptions.class;

    public class DefaultExchangeOptions implements RabbitMQExchangeOptions {

        @Override
        public Boolean durable() {
            return false;
        }

        @Override
        public Boolean exclusive() {
            return true;
        }

        @Override
        public Boolean autoDelete() {
            return false;
        }

    }
}
