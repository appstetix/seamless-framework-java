package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.annotation.*;
import com.appstetix.appstract.seamless.core.exception.custom.MalformedMethodException;
import com.appstetix.appstract.seamless.core.exception.custom.MissingHandlerException;
import com.appstetix.appstract.seamless.core.generic.AccessType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class SeamlessHandler extends AbstractVerticle {

    private static final String BASE_VERSION_URL_PATTERN = "%s:/%s/v%d/%s";
    private static final String BASE_URL_PATTERN = "%s:/%s/%s";
    private static final String VERSION_URL_PATTERN = "%s:/v%d/%s";
    private static final String URL_PATTERN = "%s:/%s";

    @Override
    public void start() throws Exception {
        evaluate();
    }

    protected SeamlessRequest getRequest(Message message) {
        if(message != null) {
            return Json.decodeValue((String) message.body(), SeamlessRequest.class);
        }
        return null;
    }

    protected <T> T getPostBody(String json, Class<T> clss) {
        if (StringUtils.isNotEmpty(json)) {
            if(StringUtils.isNotEmpty(json)) {
                return Json.decodeValue(json, clss);
            }
        }
        return null;
    }

    protected void respond(Message message, SeamlessResponse response) {
        respond(message, new DeliveryOptions(), response);
    }

    protected void respond(Message message, DeliveryOptions options, SeamlessResponse response) {
        message.reply(Json.encode(response), options);
    }

    private void evaluate() throws MissingHandlerException, MalformedMethodException {
        if(this.getClass().isAnnotationPresent(APIHandler.class)) {
            APIHandler handler = this.getClass().getAnnotation(APIHandler.class);
            Method[] methods = this.getClass().getDeclaredMethods();
            if(methods != null && methods.length > 0) {
                EventBus eb = vertx.eventBus();
                if(AccessType.ALL.equals(handler.access()) || AccessType.WEB_ONLY.equals(handler.access())) {
                    evaluateEndpoints(handler.baseURL(), methods, eb);
                }
                if(AccessType.ALL.equals(handler.access()) || AccessType.TASK_ONLY.equals(handler.access())) {
                    evaluateTasks(methods, eb);
                }
            } else {
                log.warn("No methods found for [{}]", this.getClass().getName());
            }
        } else {
            throw new MissingHandlerException(
                    String.format("No Seamless handler found for '%s'. Please annotate this class with @%s",
                        this.getClass().getName(), APIHandler.class.getSimpleName()));
        }
    }

    private void evaluateEndpoints(String baseURL, Method[] methods, EventBus eb) throws MalformedMethodException {
        if(methods != null && methods.length > 0) {
            for(Method method : methods){
                method.setAccessible(true);
                if(method.isAnnotationPresent(Endpoint.class) || method.isAnnotationPresent(Endpoints.class)){
                    validateMethod(method);
                    Endpoint[] endpoints = method.getAnnotationsByType(Endpoint.class);
                    if(endpoints != null) {
                        for(Endpoint endpoint : endpoints) {
                            String endpointUrl = getEndpointUrl(baseURL, endpoint);
                            if(!endpoint.secure()) {
                                SeamlessAPI.addToBypass(endpointUrl);
                            }
                            setupMethod(eb, method, endpointUrl);
                            log.info("CREATED ENDPOINT : {}", endpointUrl);
                        }
                    }
                } else {
                    log.debug("No endpoint found for method '{}'", method.getName());
                }
            }
        }
    }

    private void evaluateTasks(Method[] methods, EventBus eb) throws MalformedMethodException {
        for(Method method : methods){
            method.setAccessible(true);
            if(method.isAnnotationPresent(Task.class) || method.isAnnotationPresent(Tasks.class)){
                validateMethod(method);
                Task[] tasks = method.getAnnotationsByType(Task.class);
                if(tasks != null) {
                    for(Task task : tasks) {
                        if(!task.secure()) {
                            SeamlessAPI.addToBypass(task.value());
                        }
                        setupMethod(eb, method, task.value());
                        log.info("CREATED TASK : {}", task.value());
                    }
                }
            }
        }
    }

    private void setupMethod(EventBus eb, Method method, String address) {
        eb.consumer(address, message -> {
            try {
                Object result = null;
                if(method.getParameterCount() > 0) {
                    result = method.invoke(this, message);
                } else {
                    result = method.invoke(this);
                }
                if(result != null && result instanceof SeamlessResponse) {
                    respond(message, (SeamlessResponse) result);
                } else {
                    respond(message, SeamlessResponse.builder().code(200).payload( result).build());
                }
            } catch (Exception e) {
                sendErrorResponse(message, e.getCause());
            }
        });
    }

    private String getEndpointUrl(String baseURL, Endpoint endpoint) {
        if(endpoint == null) {
            throw new IllegalArgumentException("parameter [endpoint] cannot be null");
        }
        if(StringUtils.isNotEmpty(baseURL)) {
            if(endpoint.version() > 0) {
                return String.format(BASE_VERSION_URL_PATTERN, endpoint.method(), cleanPath(baseURL), endpoint.version(), cleanPath(endpoint.path()));
            } else {
                return String.format(BASE_URL_PATTERN, endpoint.method(), cleanPath(baseURL), cleanPath(endpoint.path()));
            }
        } else if(endpoint.version() > 0) {
            return String.format(VERSION_URL_PATTERN, endpoint.method(), endpoint.version(), cleanPath(endpoint.path()));
        } else {
            return String.format(URL_PATTERN, endpoint.method(), cleanPath(endpoint.path()));
        }
    }

    private String cleanPath(String path) {
        if(path != null && !path.isEmpty()) {
            String firstChar = path.trim().substring(0, 1);
            if("/".equals(firstChar)) {
                path =  path.trim().substring(1, path.length());
            }
            if(path.lastIndexOf("/") == (path.length() - 1)) {
                path = path.trim().substring(0, (path.length() - 1));
            }
            return path;
        }
        return "";
    }

    private void sendErrorResponse(Message message, Throwable e) {
        SeamlessResponse response = SeamlessResponse.builder().error(e != null ? e : new Exception()).build();
        message.reply(Json.encode(response));
    }

    private void validateMethod(Method method) throws MalformedMethodException {
        if(method.getParameterCount() > 1) {
            throw new MalformedMethodException(String.format("Method '%s' has %d parameters. Expected 1 parameter of type [%s]",
                    method.getName(), method.getParameterCount(), Message.class.getName()));
        } else if(method.getParameterCount() == 1) {
            final Class<?> parameterType = method.getParameterTypes()[0];
            if(!parameterType.getName().equals(Message.class.getName())) {
                throw new MalformedMethodException(String.format("Parameter for method '%s' needs to be of type [%s]. Found [%s]",
                        method.getName(), Message.class.getName(), parameterType.getName()));
            }
        }
    }
}
