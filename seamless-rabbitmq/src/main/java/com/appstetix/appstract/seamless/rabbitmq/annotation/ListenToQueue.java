package com.appstetix.appstract.seamless.rabbitmq.annotation;

import com.appstetix.appstract.seamless.rabbitmq.config.RabbitMQQueueOptions;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(QueueListeners.class)
public @interface ListenToQueue {

    String DEFAULT_CONNECTION = "DEFAULT_CONNECTION";

    String queueName();
    String connectionId() default DEFAULT_CONNECTION;
    Class<? extends RabbitMQQueueOptions> queueOptionsClass() default DefaultRabbitMQQueueOptions.class;


    public class DefaultRabbitMQQueueOptions implements RabbitMQQueueOptions {

        @Override
        public Boolean autoAck() {
            return null;
        }

        @Override
        public Boolean keepMostRecent() {
            return null;
        }

        @Override
        public Integer maxInternalQueueSize() {
            return null;
        }
    }


}
