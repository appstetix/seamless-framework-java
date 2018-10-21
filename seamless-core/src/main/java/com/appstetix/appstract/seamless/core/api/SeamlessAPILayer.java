package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.annotation.API;
import com.appstetix.appstract.seamless.core.annotation.APIHandler;
import com.appstetix.appstract.seamless.core.exception.APIFilterException;
import com.appstetix.appstract.seamless.core.exception.APIViolationException;
import com.appstetix.appstract.seamless.core.generic.APIValidator;
import com.appstetix.appstract.seamless.core.generic.FilterProcessor;
import com.appstetix.appstract.seamless.core.util.AnnotationUtil;
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
public abstract class SeamlessAPILayer<REQ, RESP> implements SeamlessProvider<REQ, RESP> {

    //VERTX SETTINGS
    protected static final String VERTX_DISABLE_FILE_CPRESOLVING = "vertx.disableFileCPResolving";

    static {
        System.setProperty(VERTX_DISABLE_FILE_CPRESOLVING, "true");
        options = new DeploymentOptions().setWorker(true);
        vertx = Vertx.vertx();
    }

    private static List<String> bypass = new ArrayList();

    protected static DeploymentOptions options;
    protected static Vertx vertx;

    private APIValidator validator;
    private FilterProcessor filterProcessor;
    private DeliveryOptions deliveryOptions;

    public static void addToBypass(String path) {
        bypass.add(path);
    }

    public SeamlessAPILayer() {
        try {
            deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSecureEndpoint(String path) {
        return !bypass.contains(path);
    }

    public DeliveryOptions getDeliveryOptions() {
        return deliveryOptions;
    }

    protected void executeValidator(SeamlessRequest request) throws APIViolationException {
        if(this.validator != null) {
            validator.validate(request, this);
        }
    }

    protected void executeFilters(SeamlessRequest request, Object rawInput) throws APIFilterException {
        if (this.filterProcessor != null) {
            filterProcessor.process(request, rawInput);
        }
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

    private void deploy() throws InstantiationException, IllegalAccessException {
        API api = this.getClass().getDeclaredAnnotation(API.class);
        if(api != null) {
            int handlersSetup = setupHandlers(api);
            if(handlersSetup > 0) {
                setupValidator(api);
                setupFilter(api);
                setupDeliveryOptions(api);
            }
        } else {
            System.out.println(String.format("WARNING: No @API annotation found on class [%s]", this.getClass().getName()));
        }
    }

    private int setupHandlers(API api) {
        Set<String> handlers = getHandlers(api);
        if(handlers.size() > 0) {
            handlers.forEach(handler -> {
                System.out.println(String.format("Launching handler [%s]", handler));
                launch(handler, options);
            });
            return handlers.size();
        } else {
            System.err.println("No API handlers found");
            return 0;
        }
    }

    private void setupValidator(API api) throws IllegalAccessException, InstantiationException {
        if(api != null && !API.DEFAULT_VALIDATOR.class.getName().equals(api.validator().getName())) {
            System.out.println(String.format("FOUND VALIDATOR: [%s]", api.validator().getSimpleName()));
            validator = api.validator().newInstance();
        }
    }

    private void setupFilter(API api) throws InstantiationException, IllegalAccessException {
        if(api != null && api.filters() != null && !API.DEFAULT_FILTER.class.equals(api.filters()[0])) {
            System.out.println(String.format("FOUND %d FILTERS", api.filters().length));
            filterProcessor = new FilterProcessor(api.filters());
        }
    }

    private void setupDeliveryOptions(API api) {
        this.deliveryOptions = new DeliveryOptions();
        if(api.requestTimeout() > 0) {
            this.deliveryOptions.setSendTimeout(api.requestTimeout());
        }
        if(api.requestTimeout() < 1000) {
            log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            log.warn(String.format("Your request timeout is less than 1 second [%d]. This is not recommended", api.requestTimeout()));
            log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }

    private Set<String> getHandlers(API api) {
        if(api != null) {
            if(api.handlers().length > 0) {
                System.out.println(String.format("FOUND %d HANDLERS", api.handlers().length));
                return Arrays.stream(api.handlers()).map(Class::getName).collect(Collectors.toSet());
            } else {
                return AnnotationUtil.findClassesWithAnnotation(APIHandler.class);
            }
        }
        return Collections.EMPTY_SET;
    }

    private static void launch(String verticle) {
        launch(verticle, options);
    }

    private static void launch(String verticle, DeploymentOptions options) {
        vertx.deployVerticle(verticle, options);
    }

    private static MessageCodec getMessageCodec() {
        return null;
    }

}
