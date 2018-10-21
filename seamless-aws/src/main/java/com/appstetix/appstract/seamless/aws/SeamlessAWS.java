package com.appstetix.appstract.seamless.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.appstetix.appstract.seamless.aws.factory.AWSEventFactory;
import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.exception.APIFilterException;
import com.appstetix.appstract.seamless.core.exception.APIViolationException;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.BAD_REQUEST_ERROR;
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
                executeValidator(request);
                executeFilters(request, input);
                final CompletableFuture<SeamlessResponse> future = new CompletableFuture<>();
                dispatch(request, rs -> {
                    try {
                        SeamlessResponse response;
                        if (rs.succeeded()) {
                            response = getPostBody((String) rs.result().body(), SeamlessResponse.class);
                        } else {
                            if(rs.cause() != null && rs.cause().getMessage() != null && context != null && context != null) {
                                log.error("Request id = " + context.getAwsRequestId() + " failed. Cause = " + rs.cause().getMessage());
                                response = new SeamlessResponse(500, rs.cause().getMessage());
                            } else {
                                response = new SeamlessResponse(500, "Unable to process your request at this time");
                            }
                        }
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
            } catch (APIViolationException ex) {
                ex.printStackTrace();
                return sendUnauthorizedResponse(ex);
            } catch (APIFilterException ex) {
                ex.printStackTrace();
                return sendErrorResponse(ex.getCode(), ex.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiGatewayResponse(SERVER_ERROR, e.getMessage(), null, false);
        }
    }

    @Override
    public SeamlessRequest convertRequest(Map<String, Object> input) {
        try {
            return AWSEventFactory.getInstance().createRequest(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    private ApiGatewayResponse sendUnauthorizedResponse(Exception ex) {
        return sendErrorResponse(401, ex.getMessage());
    }

    private ApiGatewayResponse sendBadRequestResponse(Exception ex) {
        return sendErrorResponse(400, ex.getMessage());
    }

    private ApiGatewayResponse sendErrorResponse(int code, Object body) {
        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", TEXT_PLAIN);
        final ApiGatewayResponse.Builder builder = ApiGatewayResponse.builder()
                .setHeaders(headers)
                .setStatusCode(code);
        if(body instanceof String) {
            builder.setObjectBody(body);
        } else {
            builder.setRawBody((String) body);
        }
        return builder.build();
    }

    private Map<String, String> getCORSHeaders() {
        Map<String, String> headers = new HashMap();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Credentials", Boolean.TRUE.toString());
        return headers;
    }

}
