package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.annotation.Endpoint;
import com.appstetix.appstract.seamless.core.annotation.APIHandler;
import com.appstetix.appstract.seamless.core.annotation.Task;
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

    private void evaluate() throws MissingHandlerException {
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

    private void evaluateEndpoints(String baseURL, Method[] methods, EventBus eb) {
        if(methods != null && methods.length > 0) {
            for(Method method : methods){
                method.setAccessible(true);
                if(method.isAnnotationPresent(Endpoint.class)){
                    Endpoint endpoint = method.getDeclaredAnnotation(Endpoint.class);
                    String endpointUrl = getEndpointUrl(baseURL, endpoint);
                    if(!endpoint.secure()) {
                        SeamlessAPI.addToBypass(endpointUrl);
                    }
                    eb.consumer(endpointUrl, message -> {
                        try {
                            method.invoke(this, message);
                        } catch (Exception e) {
                            sendErrorResponse(message, e.getCause());
                        }
                    });
                    log.info("CREATED ENDPOINT : {}", endpointUrl);
                } else {
                    log.warn("No endpoint found for method '{}'", method.getName());
                }
            }
        }
    }

    private void evaluateTasks(Method[] methods, EventBus eb) {
        for(Method method : methods){
            method.setAccessible(true);
            if(method.isAnnotationPresent(Task.class)){
                Task task = method.getDeclaredAnnotation(Task.class);
                if(!task.secure()) {
                    SeamlessAPI.addToBypass(task.value());
                }
                eb.consumer(task.value(), message -> {
                    try {
                        method.invoke(this, message);
                    } catch (Exception e) {
                        sendErrorResponse(message, e);
                    }
                });
                log.info("CREATED TASK : {}", task.value());
            }
        }
    }

    private String getEndpointUrl(String baseURL, Endpoint endpoint) {
        if(baseURL != null && !"".equals(baseURL)) {
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
        message.reply(Json.encode(new SeamlessResponse(e)));
    }

    private Map<String, String> getErrorHeaders(Exception ex) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Error", ex.getClass().getName());
        return headers;
    }
}
