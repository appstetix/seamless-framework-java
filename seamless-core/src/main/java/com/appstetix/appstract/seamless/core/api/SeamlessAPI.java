package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.annotation.API;
import com.appstetix.appstract.seamless.core.annotation.APIHandler;
import com.appstetix.appstract.seamless.core.annotation.EnableExceptionHandling;
import com.appstetix.appstract.seamless.core.exception.ExceptionResolver;
import com.appstetix.appstract.seamless.core.exception.custom.ExceptionResolverException;
import com.appstetix.appstract.seamless.core.util.AnnotationUtil;
import com.appstetix.appstract.seamless.core.validator.ValidatorProcessor;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class SeamlessAPI<REQ, RESP> implements SeamlessProvider<REQ, RESP> {

    static {
        System.setProperty("vertx.disableFileCPResolving", "true");
        options = new DeploymentOptions().setWorker(true);
    }

    public static Vertx vertx = Vertx.vertx();
    protected static DeploymentOptions options;
    private static Set<String> bypass = new HashSet();
    private static Set<String> verticles = new HashSet();

    private ValidatorProcessor validatorProcessor;
    private ExceptionResolver exceptionResolver;
    private DeliveryOptions deliveryOptions;

    public SeamlessAPI() {
        try {
            deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addToBypass(String path) {
        bypass.add(path);
    }

    public static boolean isSecureEndpoint(String path) {
        return !bypass.contains(path);
    }

    protected DeliveryOptions getDeliveryOptions() {
        return deliveryOptions;
    }

    protected void executeValidator(SeamlessRequest request) throws Exception {
        if(this.validatorProcessor != null) {
            this.validatorProcessor.process(request, this);
        }
    }

    protected SeamlessResponse resolveException(SeamlessRequest request, String errorClass, Throwable exception) {
        if(this.exceptionResolver != null) {
            return this.exceptionResolver.resolve(request, errorClass, exception);
        }
        return null;
    }

    protected void dispatch(SeamlessRequest request, Handler<AsyncResult<Message<Object>>> handler) {
        vertx.eventBus().send(request.getRequestPath(), Json.encode(request), getDeliveryOptions(), handler);
    }

    protected <T> T getPostBody(String json, Class<T> clss) {
        if (StringUtils.isNotEmpty(json)) {
            if(StringUtils.isNotEmpty(json)) {
                return Json.decodeValue(json, clss);
            }
        }
        return null;
    }

    private void deploy() throws Exception {
        API api = this.getClass().getDeclaredAnnotation(API.class);
        if(api != null) {
            int handlersSetup = setupHandlers(api);
            setupExceptionHandlers();
            if(handlersSetup > 0) {
                setupValidator(api);
                setupDeliveryOptions(api);
            }
        } else {
            log.warn(String.format("WARNING: No @API annotation found on class [%s]. The application may not work as desired", this.getClass().getName()));
        }
    }

    private int setupHandlers(API api) {
        Set<String> handlers = getHandlers(api);
        if(handlers.size() > 0) {
            handlers.forEach(handler -> {
                launch(handler);
            });
            return handlers.size();
        } else {
            log.warn("No API verticles found");
            return 0;
        }
    }

    private void setupValidator(API api) throws IllegalAccessException, InstantiationException {
        if(api != null && api.validators().length > 0) {
            this.validatorProcessor = new ValidatorProcessor(api.validators());
        }
    }

    private void setupExceptionHandlers() throws ExceptionResolverException {
        final EnableExceptionHandling enableExceptionHandling = this.getClass().getDeclaredAnnotation(EnableExceptionHandling.class);
        this.exceptionResolver = ExceptionResolver.getInstance(enableExceptionHandling);
    }

    private void setupDeliveryOptions(API api) {
        this.deliveryOptions = new DeliveryOptions();
        if(api.requestTimeout() > 0) {
            this.deliveryOptions.setSendTimeout(api.requestTimeout());
        }
        if(api.requestTimeout() > 0 && api.requestTimeout() < 1000) {
            log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            log.warn(String.format("Your request timeout is less than 1 second [%d]. This is not recommended", api.requestTimeout()));
            log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }

    private Set<String> getHandlers(API api) {
        if(api != null) {
            if(api.handlers().length > 0) {
                log.debug(String.format("FOUND %d HANDLERS", api.handlers().length));
                return Arrays.stream(api.handlers()).map(Class::getName).collect(Collectors.toSet());
            } else {
                return AnnotationUtil.findClassNamesWithAnnotation(APIHandler.class);
            }
        }
        return Collections.EMPTY_SET;
    }

    private static void launch(String verticle) {
        if(!verticles.contains(verticle)) {
            verticles.add(verticle);
            vertx.deployVerticle(verticle, options, result -> {
                if(result.succeeded()) {
                    log.info(String.format("Launched handler [%s]", verticle));
                } else {
                    vertx.undeploy(verticle, asyncResult -> {
                        verticles.remove(verticle);
                    });
                }
            });
        }
    }

}
