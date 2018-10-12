package com.appstetix.appstract.seamless.core.integration;

import com.appstetix.appstract.seamless.core.generic.SeamlessHandler;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;

public abstract class SeamlessIntegrationHandler extends SeamlessHandler {

    protected static final String ENDPOINT_PATTERN = "%s:%s";

    public SeamlessIntegrationHandler(Class clazz) {
        super(clazz);
    }

    @Override
    protected void setup() {
        logger.info(String.format("Starting up integration verticle [%s]", logger.getName()));
        registerTasks();
        logger.info(String.format("Tasks for [%s] have been deployed", logger.getName()));
    }

    protected abstract void registerTasks();

    protected void createTask(String path, Handler<Message<Object>> handler) {
        if(StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("parameter [path] cannot be null or empty");
        }
        if(handler == null) {
            throw new IllegalArgumentException("parameter [handler] cannot be null");
        }
        final String endpoint = getEndpoint(path, HttpMethod.PATCH);
        logger.info("registering task: " + endpoint);
        vertx.eventBus().consumer(endpoint, handler);
    }

    private String getEndpoint(String path, HttpMethod method) {
        return String.format(ENDPOINT_PATTERN, method, path.trim());
    }
}
