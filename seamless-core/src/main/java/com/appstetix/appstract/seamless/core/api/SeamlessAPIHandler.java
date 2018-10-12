package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.generic.SeamlessHandler;
import com.appstetix.appstract.seamless.core.generic.SeamlessResponse;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.*;

public abstract class SeamlessAPIHandler extends SeamlessHandler {

    protected static final String API_ENDPOINT_PATTERN = "%s:/%s/v%s/%s";

    protected static final String DEFAULT_BAD_REQUEST_MESSAGE = "Bad Request";
    protected static final String DEFAULT_UNAUTHORIZED_REQUEST_MESSAGE = "You're unauthorized to use this API";
    protected static final String DEFAULT_CONFLICT_ERROR_MESSAGE = "Conflict Detected";

    protected String basepath;

    public SeamlessAPIHandler(Class clazz, String basepath) {
        super(clazz);
        this.basepath = basepath.startsWith("/") ? basepath.replaceFirst("/", "") : basepath;
    }

    @Override
    protected void setup() {
        logger.info("Starting up verticle...");
        registerEndpoints();
        logger.info("...verticle deployed");
    }

    protected abstract void registerEndpoints();

    protected void createEndpoint(String path, int version, Handler<Message<Object>> handler) {
        createEndpoint(path, version, HttpMethod.GET, handler);
    }

    protected void createEndpoint(String path, int version, HttpMethod method, Handler<Message<Object>> handler) {
        createEndpoint(getEndpointPath(path, version, method.name()), handler, true);
    }

    protected void createInsecureEndpoint(String path, int version, Handler<Message<Object>> handler) {
        createInsecureEndpoint(path, version, HttpMethod.GET, handler);
    }

    protected void createInsecureEndpoint(String path, int version, HttpMethod method, Handler<Message<Object>> handler) {
        createEndpoint(getEndpointPath(path, version, method.name()), handler, false);
    }

    protected void createEndpoint(String path, Handler<Message<Object>> handler, boolean secure) {
        logger.info("registering endpoint: " + path);
        if(!secure) {
            SeamlessAPILayer.addToBypass(path);
            logger.info("Bypass Registered for: " + path);
        }
        vertx.eventBus().consumer(path, handler);
    }

    //HTTP RESPONSE HANDLERS
    protected void successfulWithNoContent(Message message) {
        successfulWithNoContent(message, null);
    }

    protected void successfulWithNoContent(Message message, Map<String, String> headers) {
        successfulResponse(message, NO_CONTENT, null, headers);
    }

    protected void successfullyCreated(Message message) {
        successfullyCreated(message, null);
    }

    protected void successfullyCreated(Message message, Object data) {
        successfullyCreated(message, data, null);
    }
    
    protected void successfullyCreated(Message message, Object data, Map<String, String> headers) {
        successfulResponse(message, CREATED, data, headers);
    }

    protected void successfullyAccepted(Message message) {
        successfullyAccepted(message, null);
    }

    protected void successfullyAccepted(Message message, Object data) {
        successfullyAccepted(message, data, null);
    }

    protected void successfullyAccepted(Message message, Object data, Map<String, String> headers) {
        successfulResponse(message, ACCEPTED, data, headers);
    }

    protected void successful(Message message) {
        successful(message, null);
    }

    protected void successful(Message message, Object data) {
        successful(message, data, null);
    }

    protected void successful(Message message, Object data, Map<String, String> headers) {
        successfulResponse(message, SUCCESSFUL, data, headers);
    }

    private void successfulResponse(Message message, int httpCode, Object data, Map<String, String> headers) {
        respond(message, new SeamlessResponse(httpCode, headers, data));
    }

    protected void badRequestResponse(Message message, String error) {
        final SeamlessResponse response = new SeamlessResponse();
        response.setCode(BAD_REQUEST_ERROR);
        response.setErrorMessage(StringUtils.isNotEmpty(error) ? error : DEFAULT_BAD_REQUEST_MESSAGE);
        respond(message, response);
    }

    protected void badRequestResponse(Message message, Object payload) {
        final SeamlessResponse response = new SeamlessResponse();
        response.setCode(BAD_REQUEST_ERROR);
        response.setPayload(payload);
        respond(message, response);
    }

    protected void unauthorizedRequestResponse(Message message, String error) {
        final SeamlessResponse response = new SeamlessResponse();
        response.setCode(UNAUTHORIZED_ERROR);
        response.setErrorMessage(StringUtils.isNotEmpty(error) ? error : DEFAULT_UNAUTHORIZED_REQUEST_MESSAGE);
        respond(message, response);
    }

    protected void unauthorizedRequestResponse(Message message, Object payload) {
        final SeamlessResponse response = new SeamlessResponse();
        response.setCode(UNAUTHORIZED_ERROR);
        response.setPayload(payload);
        respond(message, response);
    }

    protected void conflictResponse(Message message, String error) {
        final SeamlessResponse response = new SeamlessResponse();
        response.setCode(CONFLICT_ERROR);
        response.setErrorMessage(StringUtils.isNotEmpty(error) ? error : DEFAULT_CONFLICT_ERROR_MESSAGE);
        respond(message, response);
    }

    protected void conflictResponse(Message message, Object payload) {
        final SeamlessResponse response = new SeamlessResponse();
        response.setCode(CONFLICT_ERROR);
        response.setPayload(payload);
        respond(message, response);
    }

    protected String getEndpointPath(String subPath, int version, String method) {
        if (StringUtils.isEmpty(subPath)) {
            throw new IllegalArgumentException("property [subPath] cannot be null or empty");
        }
        if (StringUtils.isEmpty(method)) {
            method = "GET";
        }
        return String.format(API_ENDPOINT_PATTERN, method.toUpperCase(), basepath.trim(), String.valueOf(version), subPath.trim());
    }

}
