package com.appstetix.appstract.seamless.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.generic.SeamlessRequest;
import com.appstetix.appstract.seamless.core.generic.SeamlessResponse;
import com.appstetix.appstract.seamless.core.generic.UserContext;
import com.appstetix.toolbelt.locksmyth.keycore.exception.InvalidTokenException;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.SERVER_ERROR;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.UNAUTHORIZED_ERROR;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.*;

@Slf4j
public abstract class SeamlessAWS extends SeamlessAPILayer<Map<String, Object>, ApiGatewayResponse> implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    public SeamlessAWS() {
        BasicConfigurator.configure();
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            SeamlessRequest request = convertRequest(input);
            try {
                final UserContext userContext = securityCheck(request);
                request.setUserContext(userContext);
                final CompletableFuture<SeamlessResponse> future = new CompletableFuture<>();
                vertx.eventBus().send(request.getRequestPath(), Json.encode(request), rs -> {
                    try {
                        SeamlessResponse response;
                        if (rs.failed()) {
                            if(rs.cause() != null && rs.cause().getMessage() != null && context != null && context != null) {
                                log.error("Request id = " + context.getAwsRequestId() + " failed. Cause = " + rs.cause().getMessage());
                                response = new SeamlessResponse(500, rs.cause().getMessage());
                            } else {
                                response = new SeamlessResponse(500, "Unable to process your request at this time");
                            }
                            future.complete(response);
                        }
                        response = getPostBody((String) rs.result().body(), SeamlessResponse.class);
                        future.complete(response);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        future.complete(new SeamlessResponse(500, ex.getMessage()));
                    }
                });

                SeamlessResponse response = future.get();
                if(response.hasHeaders()) {
                    response.getHeaders().putAll(getCORSHeaders());
                } else {
                    response.setHeaders(getCORSHeaders());
                }

                return convertResponse(response);
            } catch (InvalidTokenException ex) {
                ex.printStackTrace();
                return sendUnauthorizedResponse(ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiGatewayResponse(SERVER_ERROR, e.getMessage(), null, false);
        }
    }

    @Override
    public SeamlessRequest convertRequest(Map<String, Object> input) {

        final SeamlessRequest request = new SeamlessRequest();

        final Map queryStringParameters = (Map) input.get("queryStringParameters");
        if(queryStringParameters != null && !queryStringParameters.isEmpty()) {
            queryStringParameters.forEach((key, vals) -> {
                if(vals.getClass().isArray()) {
                    ArrayList values = (ArrayList) vals;
                    values.forEach(o -> {
                        request.addParameter((String) key, o);
                    });
                } else {
                    request.addParameter((String) key, vals);
                }
            });
        }

        request.setHeaders((Map<String, String>) input.get("headers"));
        request.setMethod((String) input.get("httpMethod"));
        request.setPath((String) input.get("path"));
        request.setBody((String) input.get("body"));

        return request;
    }

    public ApiGatewayResponse convertResponse(SeamlessResponse response) {

        switch (response.getContentType()) {
            case APPLICATION_JSON : {
                return ApiGatewayResponse.builder()
                        .setStatusCode(response.getCode())
                        .setHeaders(response.getHeaders())
                        .setObjectBody(response.hasPayload() ? response.getPayload() : null)
                        .build();
            }
            case APPLICATION_OCTET_STREAM : {
                return ApiGatewayResponse.builder()
                        .setStatusCode(response.getCode())
                        .setHeaders(response.getHeaders())
                        .setBinaryBody(Base64.decodeBase64((String) response.getPayload()))
                        .build();
            }
            default : {
                return ApiGatewayResponse.builder()
                        .setStatusCode(response.getCode())
                        .setHeaders(response.getHeaders())
                        .setRawBody(response.hasPayload() ? (String) response.getPayload() : null)
                        .build();

            }
        }
    }

    private ApiGatewayResponse sendUnauthorizedResponse(InvalidTokenException ex) {
        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", TEXT_PLAIN);
        return ApiGatewayResponse.builder()
                .setHeaders(headers)
                .setStatusCode(UNAUTHORIZED_ERROR)
                .setRawBody(ex.getMessage())
                .build();
    }

    private Map<String, String> getCORSHeaders() {
        Map<String, String> headers = new HashMap();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Credentials", Boolean.TRUE.toString());
        return headers;
    }

}
