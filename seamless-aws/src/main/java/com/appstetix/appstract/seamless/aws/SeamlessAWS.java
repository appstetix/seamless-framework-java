package com.appstetix.appstract.seamless.aws;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.generic.SeamlessRequest;
import com.appstetix.appstract.seamless.core.generic.SeamlessResponse;
import com.appstetix.appstract.seamless.core.generic.UserContext;
import com.appstetix.toolbelt.locksmyth.keycore.exception.InvalidTokenException;
import io.vertx.core.json.Json;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.appstetix.appstract.seamless.core.generic.SeamlessResponse.*;

public abstract class SeamlessAWS extends SeamlessAPILayer<Map<String, Object>, ApiGatewayResponse> implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    public SeamlessAWS() {
        BasicConfigurator.configure();
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            SeamlessRequest request = convertRequest(input);
            final String authorization = request.getHeader("Authorization");
            try {
                final UserContext userContext = securityCheck(authorization, isSecureEndpoint(request.getRequestPath()));
                request.setUserContext(userContext);
                final CompletableFuture<SeamlessResponse> future = new CompletableFuture<>();
                vertx.eventBus().send(request.getRequestPath(), Json.encode(request), rs -> {
                    if (rs.failed()) {
                        logger.error("Request id = " + context.getAwsRequestId() + " failed. Cause = " + rs.cause().getMessage());
                        final SeamlessResponse response = new SeamlessResponse();
                        future.complete(response);
                    }
                    final SeamlessResponse response = getPostBody((String) rs.result().body(), SeamlessResponse.class);
                    future.complete(response);
                });
                SeamlessResponse response = future.get();
                return convertResponse(response);
            } catch (InvalidTokenException ex) {
                ex.printStackTrace();
                return sendUnauthorizedResponse(ex);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ApiGatewayResponse(SERVER_ERROR_STATUS_CODE, null, null, false);
        }
    }

    @Override
    public SeamlessRequest convertRequest(Map<String, Object> input) {
        final SeamlessRequest request = new SeamlessRequest();

        final Map queryStringParameters = (Map) input.get("queryStringParameters");
        queryStringParameters.forEach((key, vals) -> {
            request.addParameter((String) key, vals);
        });

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
                .setStatusCode(UNAUTHORIZED_STATUS_CODE)
                .setRawBody(ex.getMessage())
                .build();
    }

}
