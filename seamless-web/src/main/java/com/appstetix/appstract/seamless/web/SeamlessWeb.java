package com.appstetix.appstract.seamless.web;

import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.generic.SeamlessRequest;
import com.appstetix.appstract.seamless.core.generic.SeamlessResponse;
import com.appstetix.appstract.seamless.core.generic.UserContext;
import com.appstetix.toolbelt.locksmyth.keycore.exception.InvalidTokenException;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SeamlessWeb extends SeamlessAPILayer<RoutingContext, HttpServerResponse> implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        try {
            SeamlessRequest request = convertRequest(context);
            try {
                final String authorization = context.request().getHeader("Authorization");
                final UserContext userContext = securityCheck(authorization, isSecureEndpoint(request.getRequestPath()));
                request.setUserContext(userContext);
                process(context, request);
            } catch (InvalidTokenException ex) {
                ex.printStackTrace();
                context.response().setStatusCode(UNAUTHORIZED_STATUS_CODE).end("Unable to identify user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.response().setStatusCode(SERVER_ERROR_STATUS_CODE).end("Internal Server Error");
        }
    }

    @Override
    public SeamlessRequest convertRequest(RoutingContext context) {
        SeamlessRequest serverlessRequest = new SeamlessRequest();

        Map<String, String> headers = new HashMap<>();
        context.request().headers().forEach(entry -> {
            headers.put(entry.getKey(), entry.getValue());
        });

        Map<String, String> parameters = new HashMap<>();
        context.request().params().forEach(entry -> {
            parameters.put(entry.getKey(), entry.getValue());
        });

        serverlessRequest.setHeaders(headers);
        serverlessRequest.setParameters(parameters);
        serverlessRequest.setMethod(context.request().rawMethod());
        serverlessRequest.setPath(context.request().path());
        serverlessRequest.setBody(context.getBodyAsString());

        return serverlessRequest;
    }

    @Override
    public HttpServerResponse convertResponse(SeamlessResponse response) {
        return null;
    }

    protected void process(RoutingContext context, SeamlessRequest request) {
        System.out.println("Request made to : " + request.getRequestPath());
        vertx.eventBus().send(request.getRequestPath(), Json.encode(request), rs -> {
            if(rs.failed()) {
                log.error("Request id = " + context.request().path() + " failed. Cause = " + rs.cause().getMessage());
                context.response().setStatusCode(SERVER_ERROR_STATUS_CODE).end(rs.cause().getMessage());
            } else {
                final SeamlessResponse response = getPostBody(rs.result().body().toString(), SeamlessResponse.class);
                final HttpServerResponse httpServerResponse = context.response().setStatusCode(response.getCode());
                if(response.hasHeaders()) {
                    response.getHeaders().forEach((s, s2) -> {
                        httpServerResponse.putHeader(s, s2);
                    });
                }
                if(response.isSuccessful()) {
                    httpServerResponse.end(Json.encode(response.getPayload()));
                } else {
                    httpServerResponse.end(Json.encode(response.getErrorMessage()));
                }
            }
        });
    }


}
