package com.appstetix.appstract.seamless.aws.factory;

import com.appstetix.appstract.seamless.aws.ApiGatewayResponse;
import com.appstetix.appstract.seamless.aws.factory.handler.ApiEventHandler;
import com.appstetix.appstract.seamless.aws.factory.handler.IntegrationEventHandler;
import com.appstetix.appstract.seamless.aws.factory.handler.ScheduledEventHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import org.apache.commons.codec.binary.Base64;

import java.util.Map;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.APPLICATION_JSON;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.APPLICATION_OCTET_STREAM;

public final class AWSEventFactory {

    private static AWSEventFactory instance = new AWSEventFactory();
    private AWSEventHandler entrypoint;

    private AWSEventFactory() {

        ApiEventHandler apiEventHandler = new ApiEventHandler("path");
        IntegrationEventHandler integrationEventHandler = new IntegrationEventHandler("Records");
        ScheduledEventHandler scheduledEventHandler = new ScheduledEventHandler("source");

        apiEventHandler.setSuccessor(integrationEventHandler);
        integrationEventHandler.setSuccessor(scheduledEventHandler);

        this.entrypoint = apiEventHandler;
    }

    public static AWSEventFactory getInstance() {
        return instance;
    }

    public SeamlessRequest createRequest(Map<String, Object> event) throws Exception {
        if(event == null || event.isEmpty()) {
            throw new IllegalArgumentException("parameter [event] cannot be null");
        }
        return entrypoint.getRequest(event);
    }

    public ApiGatewayResponse createResponse(SeamlessResponse response) {
        if(response != null) {
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
        return ApiGatewayResponse.builder()
                .setStatusCode(500)
                .setRawBody("Unable to process request at this time")
                .build();
    }
}
