package com.appstetix.appstract.seamless.web;

import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.generic.SeamlessRequest;
import com.appstetix.appstract.seamless.core.generic.SeamlessResponse;
import com.appstetix.appstract.seamless.core.generic.UserContext;
import com.appstetix.toolbelt.locksmyth.keycore.exception.InvalidTokenException;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;
import java.util.Map;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.SERVER_ERROR;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.UNAUTHORIZED_ERROR;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.*;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.X_FORWARDED_FOR;

@Slf4j
public class SeamlessWeb extends SeamlessAPILayer<RoutingContext, HttpServerResponse> implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        try {
            SeamlessRequest request = convertRequest(context);
            try {
                final UserContext userContext = securityCheck(request);
                request.setUserContext(userContext);
                process(context, request);
            } catch (InvalidTokenException ex) {
                ex.printStackTrace();
                context.response().setStatusCode(UNAUTHORIZED_ERROR).end("Unable to identify user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.response().setStatusCode(SERVER_ERROR).end("Internal Server Error");
        }
    }

    @Override
    public SeamlessRequest convertRequest(RoutingContext context) {
        SeamlessRequest serverlessRequest = new SeamlessRequest();

        Map<String, String> headers = new HashMap<>();
        context.request().headers().forEach(entry -> {
            headers.put(entry.getKey(), entry.getValue());
        });
        String ipAddress = context.request().getHeader(X_FORWARDED_FOR);
        if (ipAddress == null) {
            ipAddress = context.request().remoteAddress().host();
        }
        headers.put(X_FORWARDED_FOR, ipAddress);

        Map<String, Object> parameters = new HashMap<>();
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
                context.response().setStatusCode(SERVER_ERROR).end(rs.cause().getMessage());
            } else {
                final SeamlessResponse response = getPostBody(rs.result().body().toString(), SeamlessResponse.class);
                final HttpServerResponse httpServerResponse = context.response().setStatusCode(response.getCode());
                if(response.hasHeaders()) {
                    response.getHeaders().forEach((s, s2) -> {
                        httpServerResponse.putHeader(s, s2);
                    });
                }
                if(response.hasPayload()) {
                    if(TEXT_PLAIN.equals(response.getContentType()) || TEXT_HTML.equals(response.getContentType())) {
                        httpServerResponse.end((String) response.getPayload());
                    } else if(APPLICATION_OCTET_STREAM.equals(response.getContentType())) {
                        byte[] bytes = Base64.decodeBase64((String) response.getPayload());
                        httpServerResponse.end(Buffer.buffer(bytes));
                    } else {
                        httpServerResponse.end(Json.encode(response.getPayload()));
                    }
                } else if(response.hasErrorMessage()) {
                    httpServerResponse.end(Json.encode(response.getErrorMessage()));
                } else {
                    httpServerResponse.end();
                }
            }
        });
    }


}
