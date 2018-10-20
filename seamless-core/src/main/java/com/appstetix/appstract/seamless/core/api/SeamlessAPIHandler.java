package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.generic.SeamlessHandler;
import com.appstetix.appstract.seamless.core.generic.SeamlessResponse;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.*;

public abstract class SeamlessAPIHandler extends SeamlessHandler {

    protected static final String DEFAULT_BAD_REQUEST_MESSAGE = "Bad Request";
    protected static final String DEFAULT_UNAUTHORIZED_REQUEST_MESSAGE = "You're unauthorized to use this API";
    protected static final String DEFAULT_CONFLICT_ERROR_MESSAGE = "Conflict Detected";

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

}
