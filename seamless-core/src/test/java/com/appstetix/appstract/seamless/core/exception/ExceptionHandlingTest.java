package com.appstetix.appstract.seamless.core.exception;

import com.appstetix.appstract.seamless.core.annotation.*;
import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.core.exception.handler.DefaultExceptionHandler;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RunWith(VertxUnitRunner.class)
public class ExceptionHandlingTest {

    private static TestAPI api;

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        api = new TestAPI();
    }

    @Test
    public void testDefaultExceptionHandler(TestContext context) {
        final Async async = context.async();
        try {
            final SeamlessResponse response = api.test("testDefaultExceptionHandler");
            context.assertNotNull(response);
            async.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
            context.fail();
            async.complete();
        }
    }

    @Test
    public void testUnknownErrorExceptionHandler(TestContext context) {
        final Async async = context.async();
        try {
            final SeamlessResponse response = api.test("GET:/ex/default");
            context.assertNotNull(response);
            context.assertEquals(500, response.getCode());
            context.assertEquals(DefaultExceptionHandler.DEFAULT_ERROR_MESSAGE, response.getPayload());
            async.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
            context.fail();
            async.complete();
        }
    }

    @Test
    public void testCustomExceptionHandler(TestContext context) {
        final Async async = context.async();
        try {
            final SeamlessResponse response = api.test("GET:/ex/custom");
            context.assertNotNull(response);
            context.assertEquals(512, response.getCode());
            context.assertEquals(TestAPIHandler.EXCEPTION_MESSAGE, response.getPayload());
            async.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
            context.fail();
            async.complete();
        }
    }

    @API(handlers = TestAPIHandler.class)
    @EnableExceptionHandling
    public static class TestAPI extends SeamlessAPI {

        public SeamlessResponse test(String path) {
            try {
                final CompletableFuture<SeamlessResponse> future = new CompletableFuture<>();
                SeamlessRequest request = convertRequest(null);
                vertx.eventBus().send(path, null, result -> {
                    SeamlessResponse response = null;
                    try {
                        if(result.succeeded()) {
                            response = (SeamlessResponse) getPostBody(result.result().body().toString(), SeamlessResponse.class);
                            if(response.hasError()) {
                                response = resolveException(request, response.getErrorClass(), response.getError());
                            }
                        } else {
                            response = resolveException(request, result.cause().getClass().getName(), result.cause());
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        future.complete(response);
                    }
                });
                SeamlessResponse response = future.get();
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public SeamlessRequest convertRequest(Object request) {
            return new SeamlessRequest();
        }

        @Override
        public Object convertResponse(SeamlessResponse response) {
            return null;
        }
    }

    @APIHandler(baseURL = "ex")
    public static class TestAPIHandler extends SeamlessHandler {

        public static final String EXCEPTION_MESSAGE = "Some Custom Exception Message";

        @Endpoint(path = "default")
        public void throwDefaultException(Message message) throws Exception {
            throw new Exception();
        }

        @Endpoint(path = "custom")
        public void throwCustomException(Message message) throws Exception {
            throw new CustomException(EXCEPTION_MESSAGE);
        }

    }

    @APIException(CustomException.class)
    public static class CustomExceptionHandler implements ExceptionHandler<String> {

        @Override
        public int responseCode(SeamlessRequest request, Throwable exception) {
            return 512;
        }

        @Override
        public Map<String, String> headers(SeamlessRequest request, Throwable exception) {
            return null;
        }

        @Override
        public String body(SeamlessRequest request, Throwable exception) {
            return exception.getMessage();
        }
    }

    public static class CustomException extends Exception {

        public CustomException() {
        }

        public CustomException(String message) {
            super(message);
        }

        public CustomException(String message, Throwable cause) {
            super(message, cause);
        }

        public CustomException(Throwable cause) {
            super(cause);
        }

        public CustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
