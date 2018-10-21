package com.appstetix.appstract.seamless.web;

import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.core.exception.APIFilterException;
import com.appstetix.appstract.seamless.core.exception.APIViolationException;
import com.appstetix.appstract.seamless.web.annotation.CORS;
import com.appstetix.appstract.seamless.web.annotation.WebServer;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.SERVER_ERROR;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.UNAUTHORIZED_ERROR;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.*;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.X_FORWARDED_FOR;

@Slf4j
public class SeamlessWeb extends SeamlessAPILayer<RoutingContext, HttpServerResponse> implements Handler<RoutingContext> {

    public SeamlessWeb() {
        super();
        starWebServer();
    }

    @Override
    public void handle(RoutingContext context) {
        try {
            SeamlessRequest request = convertRequest(context);
            try {
                executeValidator(request);
                executeFilters(request, context.request());
                process(context, request);
            } catch (APIViolationException ex) {
                ex.printStackTrace();
                context.response().setStatusCode(UNAUTHORIZED_ERROR).end("Unable to identify user");
            } catch (APIFilterException ex) {
                ex.printStackTrace();
                context.response().setStatusCode(ex.getCode()).end(ex.getMessage());
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
        log.info("Request made to : " + request.getRequestPath());
        dispatch(request, rs -> {
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
                } else {
                    httpServerResponse.end();
                }
            }
        });
    }

    private void starWebServer() {
        final WebServer webServer = this.getClass().getAnnotation(WebServer.class);
        HttpServer server = vertx.createHttpServer();
        Router router = setupRouter();
        router.route().handler(this);
        server.requestHandler(router::accept).listen(webServer != null ? webServer.port() : 8888);
    }

    private Router setupRouter() {
        Router router = Router.router(vertx);
        setupCors(router);
        router.route().handler(BodyHandler.create());
        return router;
    }


    private void setupCors(Router router) {
        CORS cors = this.getClass().getAnnotation(CORS.class);
        if(cors != null) {
            System.out.println("Configuring Cors");
            CorsHandler handler = CorsHandler.create(cors.origins());
            if(cors.methods().length > 0) {
                handler.allowedMethods(Arrays.stream(cors.methods()).collect(Collectors.toSet()));
            }
            if(cors.headers().length > 0) {
                handler.allowedHeaders(Arrays.stream(cors.headers()).collect(Collectors.toSet()));
            }
            router.route().handler(handler);
        }
    }
}
