package com.appstetix.appstract.seamless.web;

import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.web.annotation.CORS;
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
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.*;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.X_FORWARDED_FOR;

@Slf4j
public class SeamlessWeb extends SeamlessAPI<RoutingContext, HttpServerResponse> implements Handler<RoutingContext> {

    public SeamlessWeb() {
        super();
        starWebServer();
    }

    @Override
    public void handle(RoutingContext context) {
        try {
            SeamlessRequest request = convertRequest(context);
            executeValidator(request);
            process(context, request);
        } catch (Exception e) {
            SeamlessResponse response = resolveException(null, e.getClass().getName(), e);
            final HttpServerResponse httpServerResponse = context.response();
            if(response.hasHeaders()) {
                response.getHeaders().forEach((s, s2) -> {
                    httpServerResponse.putHeader(s, s2);
                });
            }
            httpServerResponse.setStatusCode(response.getCode())
                    .end(Json.encodeToBuffer(response.getPayload()));
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
        log.info("Request made to : {}", request.getRequestPath());
        dispatch(request, rs -> {
            final HttpServerResponse httpServerResponse = context.response();
            SeamlessResponse response = null;
            try {
                if(rs.failed()) {
                    log.error("Request to = '{}' failed. Cause = {}", context.request().path(), rs.cause().getMessage());
                    response = resolveException(request, rs.cause().getClass().getName(), rs.cause());
                } else {
                    response = getPostBody(rs.result().body().toString(), SeamlessResponse.class);
                    if(response.hasError()) {
                        response = resolveException(request, response.getErrorClass(), response.getError());
                    }
                }
            } catch (Exception ex) {
                response = resolveException(request, ex.getClass().getName(), ex);
            } finally {
                finalizeResponse(httpServerResponse, response);
            }
        });
    }

    private void starWebServer() {
        HttpServer server = vertx.createHttpServer();
        Router router = setupRouter();
        router.route().handler(this);
        server.requestHandler(router::accept).listen(SeamlessWebProperties.APPLICATION_PORT);
        log.info(String.format("Started web server on port : %d", SeamlessWebProperties.APPLICATION_PORT));
        log.info("=================================================");
        log.info("            SEAMLESS SERVICE DEPLOYED            ");
        log.info("=================================================");
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
            log.info("CORS CONFIGURED");
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

    private void finalizeResponse(HttpServerResponse httpServerResponse, SeamlessResponse response) {
        httpServerResponse.setStatusCode(response.getCode());
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
}
