package com.appstetix.appstract.seamless.core.generic;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.ResponseCode.SERVER_ERROR;

public abstract class SeamlessHandler extends AbstractVerticle {

    protected static final String DEFAULT_ERROR_MESSAGE = "Unable to service your request at this time";

    protected Logger logger;

    public SeamlessHandler(Class clazz) {
        logger = Logger.getLogger(clazz.getName());
    }

    @Override
    public void start() throws Exception {
        setup();
    }

    protected abstract void setup();
    protected abstract void reportIncident(Message message, Exception e);

    protected SeamlessRequest getRequest(Message message) {
        if(message != null) {
            return Json.decodeValue((String) message.body(), SeamlessRequest.class);
        }
        return null;
    }

    protected <T> T getPostBody(String json, Class<T> clss) {
        if (StringUtils.isNotEmpty(json)) {
            if(StringUtils.isNotEmpty(json)) {
                return Json.decodeValue(json, clss);
            }
        }
        return null;
    }

    protected void respond(Message message, SeamlessResponse response) {
        message.reply(Json.encode(response));
    }

    protected void respond(Message message, DeliveryOptions options, SeamlessResponse response) {
        message.reply(Json.encode(response), options);
    }

    protected void reportServerError(Message message, Exception ex) {
        logError(ex);
        reportIncident(message, ex);
        if(ex == null) {
            message.fail(SERVER_ERROR, ex.getMessage());
        } else {
            message.fail(SERVER_ERROR, DEFAULT_ERROR_MESSAGE);
        }
    }

    protected void logError(Exception e) {
        if (logger != null) {
            logger.log(Level.SEVERE, getStackTraceString(e));
        }
    }

    private static String getStackTraceString(Throwable ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

}
