package com.appstetix.appstract.seamless.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.appstetix.appstract.seamless.aws.factory.AWSEventFactory;
import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.SERVER_ERROR;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.*;

@Slf4j
public abstract class SeamlessAWS extends SeamlessAPI<Map<String, Object>, ApiGatewayResponse> implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    public SeamlessAWS() {
        BasicConfigurator.configure();
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            SeamlessRequest request = convertRequest(input);
            executeValidator(request);
            final CompletableFuture<SeamlessResponse> future = new CompletableFuture<>();
            dispatch(request, rs -> {
                try {
                    SeamlessResponse response;
                    if (rs.succeeded()) {
                        response = getPostBody((String) rs.result().body(), SeamlessResponse.class);
                    } else {
                        log.error("Request id = {} failed. Cause = {}", context.getAwsRequestId(), rs.cause().getMessage());
                        response = resolveException(request, rs.cause().getClass().getName(), rs.cause());
                    }
                    future.complete(response);
                } catch (Exception ex) {
                    future.complete(resolveException(request, ex.getClass().getName(), rs.cause()));
                }
            });

            SeamlessResponse response = future.get();
            if(response.hasHeaders()) {
                response.getHeaders().putAll(getCORSHeaders());
            } else {
                response.setHeaders(getCORSHeaders());
            }

            return convertResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return convertResponse(SeamlessResponse.builder().error(e).build());
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

    private Map<String, String> getCORSHeaders() {
        Map<String, String> headers = new HashMap();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Credentials", Boolean.TRUE.toString());
        return headers;
    }

}
