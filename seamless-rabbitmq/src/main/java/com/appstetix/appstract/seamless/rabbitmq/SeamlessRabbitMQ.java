package com.appstetix.appstract.seamless.rabbitmq;

import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.rabbitmq.annotation.*;
import com.appstetix.appstract.seamless.rabbitmq.config.RabbitMQConnectionOptions;
import com.appstetix.appstract.seamless.rabbitmq.config.RabbitMQExchangeOptions;
import com.appstetix.appstract.seamless.rabbitmq.config.RabbitMQQueueOptions;
import com.appstetix.appstract.seamless.rabbitmq.factory.ClientFactory;
import com.appstetix.appstract.seamless.rabbitmq.payload.MessageReport;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.appstetix.appstract.seamless.rabbitmq.config.SeamlessRabbitMQProperties.*;

@Slf4j
public class SeamlessRabbitMQ extends SeamlessAPI<RabbitMQMessage, MessageReport> {

    private Map<String, RabbitMQClient> clients = new HashMap();
    private int connected;

    public SeamlessRabbitMQ() {
        super();
        setup();
    }

    private void setup() {
        try {
            createClients();
            if(!clients.isEmpty()) {
                clients.forEach((id, client) -> {
                    client.start(voidAsyncResult -> {
                        if(voidAsyncResult.succeeded()) {
                            log.info("--- SUCCESSFULLY CONNECT TO RABBIT MQ HOST [{}] ---", id);
                            connected++;
                            setupQueuesAndExchanges();
                        } else {
                            log.error("xx FAILED TO CONNECT TO RABBIT MQ HOST [{}] xxx", id);
                        }
                    });
                });
            } else {
                log.error("No rabbit MQ connection settings found. Please refer to the documentation fro Seamless Rabbit MQ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupQueuesAndExchanges() {
        if(connected == this.clients.size()) {
            try {
                setupQueueListeners();
                setupExchangeListeners();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupQueueListeners() throws InstantiationException, IllegalAccessException {
        if(hasAnnotation(ListenToQueue.class) || hasAnnotation(QueueListeners.class)) {
            ListenToQueue[] queueListeners = this.getClass().getDeclaredAnnotationsByType(ListenToQueue.class);
            if(queueListeners != null) {
                for(ListenToQueue listener : queueListeners) {
                    if(StringUtils.isNotEmpty(listener.queueName())) {
                        QueueOptions options = getQueueOptions(listener.queueOptionsClass());
                        final RabbitMQClient client = getClient(listener.connectionId());
                        if(client != null) {
                            consumeQueueMessages(client, listener.queueName(), options);
                        } else {
                            log.error("No client found with the connection ID [{}]. " +
                                    "Make sure you've sent to correct environment variables or set the 'connectionId' of the " +
                                        "@{} annotation", listener.connectionId(), ListenToQueue.class.getSimpleName());
                        }
                    } else {
                        throw new IllegalArgumentException("The 'queueName' property for the @ListenToQueue annotation cannot be null or empty");
                    }
                }
            }
        }
    }

    private void setupExchangeListeners() throws InstantiationException, IllegalAccessException {
        if(hasAnnotation(ListenToExchange.class) || hasAnnotation(ExchangeListeners.class)) {
            ListenToExchange[] exchangeListeners = this.getClass().getDeclaredAnnotationsByType(ListenToExchange.class);
            if(exchangeListeners != null) {
                for(ListenToExchange listener : exchangeListeners) {
                    if(StringUtils.isNotEmpty(listener.exchangeName())) {
                        String queueName = StringUtils.isNotEmpty(listener.queueName()) ? listener.queueName() : UUID.randomUUID().toString();
                        final RabbitMQExchangeOptions rabbitMQExchangeOptions = listener.exchangeOptions().newInstance();
                        final RabbitMQClient client = getClient(listener.connectionId());
                        if(client != null) {
                            client.queueDeclare(queueName, rabbitMQExchangeOptions.durable(), rabbitMQExchangeOptions.exclusive(), rabbitMQExchangeOptions.autoDelete(), jsonObjectAsyncResult -> {
                                try {
                                    if(jsonObjectAsyncResult.succeeded()) {
                                        log.info(jsonObjectAsyncResult.result().encodePrettily());
                                        client.queueBind(queueName, listener.exchangeName(), listener.routingKey(), result -> {
                                            try {
                                                if(result.succeeded()) {
                                                    log.info("Binding between queue [{}] and exchange [{}] was successful", queueName, listener.exchangeName());
                                                    QueueOptions options = getQueueOptions(listener.queueOptionsClass());
                                                    consumeQueueMessages(client, queueName, options);
                                                } else {
                                                    log.error("Unable to bind to queue [{}]: {}", queueName, result.cause());
                                                }
                                            } catch(Exception ex) {
                                                log.error("Error while binding to queue [{}]: {}", queueName, ex);
                                                ex.printStackTrace();
                                            }
                                        });
                                    }
                                } catch(Exception ex) {
                                    log.error("Error while declaring queue [{}]: {}", queueName, ex);
                                    ex.printStackTrace();
                                }
                            });
                        } else {
                            log.error("No client found with the connection ID [{}]. " +
                                    "Make sure you've sent to correct environment variables or set the 'connectionId' of the " +
                                    "@{} annotation", listener.connectionId(), ListenToExchange.class.getSimpleName());
                        }
                    } else {
                        throw new IllegalArgumentException("The 'exchangeName' property for the @ListenToExchange annotation cannot be null or empty");
                    }
                }
            }
        }
    }

    protected RabbitMQClient getClient(String id) {
        return !this.clients.isEmpty() ? clients.get(id) : null;
    }

    private QueueOptions getQueueOptions(Class<? extends RabbitMQQueueOptions> clazz) throws InstantiationException, IllegalAccessException {
        QueueOptions options = new QueueOptions();
        if(!clazz.getName().equals(ListenToQueue.DefaultRabbitMQQueueOptions.class)) {
            RabbitMQQueueOptions rabbitMQQueueOptions = clazz.newInstance();
            options = createOptions(rabbitMQQueueOptions);
        }
        return options;
    }

    private void consumeQueueMessages(RabbitMQClient client, String queueName, QueueOptions options) {
        client.basicConsumer(queueName, options, consumer -> {
            if(consumer.succeeded()) {
                log.info("Registering handler for queue [{}]", queueName);
                final RabbitMQConsumer rabbitMQConsumer = consumer.result();
                rabbitMQConsumer.handler(message -> {
                    try {
                        log.info("Message received: " + message.envelope().routingKey());
                        this.onMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).endHandler(aVoid -> {
                    log.info("End of queue handler");
                }).exceptionHandler(throwable -> {
                    log.error("Exception Handler: {}", throwable.getMessage());
                });
            } else {
                log.error("Error registering consumer: {}", consumer.cause().getMessage());
            }
        });
    }

    @Override
    public SeamlessRequest convertRequest(RabbitMQMessage message) {
        SeamlessRequest request = new SeamlessRequest();
        request.setPath(message.envelope().routingKey());
        //set body
        request.setBody(message.body().toString());
        //headers
        Map<String, String> headers = new HashMap();
        if(message.properties() != null && message.properties().headers() != null) {
            message.properties().headers().forEach((s, o) -> {
                headers.put(s, String.valueOf(o));
            });
        }
        //add other properties to the header
        headers.put("exchange", message.envelope().exchange());
        headers.put("consumerTag", message.consumerTag());
        headers.put("deliveryTag", String.valueOf(message.envelope().deliveryTag()));
        //add parameters
        populateParameters(request, message);
        return request;
    }

    protected void onMessage(RabbitMQMessage message) throws Exception {
        SeamlessRequest request = convertRequest(message);
        dispatch(request, result -> {
            SeamlessResponse response = null;
            try {
                if(result.succeeded()) {
                    response = getPostBody(result.result().body().toString(), SeamlessResponse.class);
                    if(response.hasError()) {
                        response = resolveException(request, response.getErrorClass(), response.getError());
                    }
                } else {
                    log.error("Request to = '{}' failed. Cause = {}", request.getPath(), result.cause().getMessage());
                    response = resolveException(request, result.cause().getClass().getName(), result.cause());
                }
            } catch (Exception ex) {
                response = resolveException(request, ex.getClass().getName(), ex);
            } finally {
                finalizeResponse(request, response);
            }
        });
    }

    @Override
    public MessageReport convertResponse(SeamlessResponse response) {
        MessageReport report = new MessageReport();
        report.setHeaders(response.getHeaders());
        report.setBody(response.getPayload());
        return report;
    }

    private void finalizeResponse(SeamlessRequest request, SeamlessResponse response) {
        MessageReport report = convertResponse(response);
        report.setQueue(request.getPath());
        String msg = response.isSuccessful() ? "Message processed successfully" : "failed to process message";
        if(response.hasPayload()) {
            report.setBody(response.getPayload());
        } else {
            report.setBody(msg);
        }

        log.info("MESSAGE REPORT : " + Json.encode(report));
    }

    private void createClients() throws IllegalAccessException, InstantiationException {

        //Find declared queue connections
        if(hasAnnotation(QueueConnection.class) || hasAnnotation(ConnectionListeners.class)) {
            final QueueConnection[] connections = getClass().getDeclaredAnnotationsByType(QueueConnection.class);
            if(connections != null) {
                for(QueueConnection connection : connections) {
                    RabbitMQClient client;
                    //Either create client using the given class or use the options directly on the @QueueConnection annotation
                    if(QueueConnection.DefaultConnectionOptions.class.getName().equals(connection.connectionClass().getName())) {
                        client = ClientFactory.create(vertx, connection.host(), connection.port(), connection.user(), connection.password(), connection.vHost());
                    } else {
                        //TODO: possible refactor
                        final RabbitMQConnectionOptions connectionOptions = connection.connectionClass().newInstance();
                        final JsonObject jsonOptions = new JsonObject(connectionOptions.options());
                        client = ClientFactory.create(vertx, new RabbitMQOptions(jsonOptions));
                    }
                    this.clients.put(connection.id(), client);
                }
            }
        }
        //Find default connection from environment variables
        if(!"null".equalsIgnoreCase(HOST)) {

            RabbitMQOptions config = new RabbitMQOptions();
            config.setHost(HOST);
            config.setPort(PORT);
            config.setUser(USER);
            config.setPassword(PASSWORD);
            config.setVirtualHost(VIRTUAL_HOST);
            config.setConnectionTimeout(6000); // in milliseconds
            config.setRequestedHeartbeat(60); // in seconds
            config.setHandshakeTimeout(6000); // in milliseconds
            config.setRequestedChannelMax(5);
            config.setNetworkRecoveryInterval(500); // in milliseconds
            config.setAutomaticRecoveryEnabled(true);

            this.clients.put(ListenToQueue.DEFAULT_CONNECTION, ClientFactory.create(vertx, config));
        }
    }

    private void populateParameters(SeamlessRequest request, RabbitMQMessage message) {
        if(message.properties() != null) {
            request.addParameter("appId", message.properties().appId());
            request.addParameter("userId", message.properties().userId());
            request.addParameter("clusterId", message.properties().clusterId());
            request.addParameter("messageId", message.properties().messageId());
            request.addParameter("contentType", message.properties().contentType());
            request.addParameter("contentEncoding", message.properties().contentEncoding());
            request.addParameter("correlationId", message.properties().correlationId());
            request.addParameter("deliveryMode", message.properties().deliveryMode());
            request.addParameter("expiration", message.properties().expiration());
            request.addParameter("priority", message.properties().priority());
            request.addParameter("replyTo", message.properties().replyTo());
//            request.addParameter("timestamp", message.properties().timestamp());
            request.addParameter("type", message.properties().type());
        }
    }

    private QueueOptions createOptions(RabbitMQQueueOptions rabbitMQOptions) {
        QueueOptions options = new QueueOptions();
        if(rabbitMQOptions.autoAck() != null) {
            options.setAutoAck(rabbitMQOptions.autoAck());
        }
        if(rabbitMQOptions.keepMostRecent() != null) {
            options.setKeepMostRecent(rabbitMQOptions.keepMostRecent());
        }
        if(rabbitMQOptions.maxInternalQueueSize() != null) {
            options.setMaxInternalQueueSize(rabbitMQOptions.maxInternalQueueSize());
        }
        return options;
    }

    private boolean hasAnnotation(Class clazz) {
        return getClass().isAnnotationPresent(clazz);
    }
}
