package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.annotation.APIHandler;
import com.appstetix.appstract.seamless.core.annotation.Endpoint;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class APIHandlerEndpointWithoutBaseURLTest {
    private static final String VERSION_AND_NO_PATH = "versionAndNoPath";
    private static final String NO_VERSION_AND_NO_PATH = "noVersionAndNoPath";
    private static final String NO_VERSION_AND_PATH = "noVersionAndPath";
    private static final String DEFAULT_METHOD = HttpMethod.GET.toString();
    private static final String VERSION_AND_PATH = "versionAndPath";
    private static EventBus eb;

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        eb = SeamlessAPI.vertx.eventBus();
        SeamlessAPI.vertx.deployVerticle(TestEndpointHandlerWithoutBaseURL.class.getName());
    }

    @Test
    public void testEndpointWithNoHandler(TestContext context) {
        final Async async = context.async();

        String URL = getURL(null, 0, "testEndpointWithNoHandler");

        eb.send(URL, null, messageAsyncResult -> {
            try {
                context.assertTrue(messageAsyncResult.failed());
                context.assertFalse(messageAsyncResult.succeeded());
                context.assertEquals(String.format("No handlers for address %s", URL), messageAsyncResult.cause().getMessage());
            } catch(Exception ex) {
                context.fail();
            }  finally {
                async.complete();
            }
        });
    }

    @Test
    public void testEndpointWithoutBaseURLAndNoPath(TestContext context) {
        final Async async = context.async();

        String url = getURL("");
        eb.send(url, null, messageAsyncResult -> {
            try {
                final SeamlessResponse response = getResponse(messageAsyncResult.result());
                context.assertTrue(messageAsyncResult.succeeded());
                context.assertEquals(NO_VERSION_AND_NO_PATH, response.getPayload());
            } catch(Exception ex) {
                context.fail();
            }  finally {
                async.complete();
            }
        });
    }

    @Test
    public void testEndpointWithoutBaseURLAndPath(TestContext context) {
        final Async async = context.async();

        String url = getURL(NO_VERSION_AND_PATH);
        eb.send(url, null, messageAsyncResult -> {
            try {
                final SeamlessResponse response = getResponse(messageAsyncResult.result());
                context.assertTrue(messageAsyncResult.succeeded());
                context.assertEquals(NO_VERSION_AND_PATH, response.getPayload());
            } catch(Exception ex) {
                context.fail();
            }  finally {
                async.complete();
            }
        });
    }

    @Test
    public void testEndpointWithoutBaseURLAndVersion(TestContext context) {
        final Async async = context.async();

        String url = getURL(null, 1);
        eb.send(url, null, messageAsyncResult -> {
            try {
                final SeamlessResponse response = getResponse(messageAsyncResult.result());
                context.assertTrue(messageAsyncResult.succeeded());
                context.assertEquals(VERSION_AND_NO_PATH, response.getPayload());
            } catch(Exception ex) {
                context.fail();
            }  finally {
                async.complete();
            }
        });
    }

    @Test
    public void testEndpointWithoutBaseURLAndVersionAndPath(TestContext context) {
        final Async async = context.async();

        String url = getURL(VERSION_AND_PATH, 1);
        eb.send(url, null, messageAsyncResult -> {
            try {
                final SeamlessResponse response = getResponse(messageAsyncResult.result());
                context.assertTrue(messageAsyncResult.succeeded());
                context.assertEquals(VERSION_AND_PATH, response.getPayload());
            } catch(Exception ex) {
                context.fail();
            }  finally {
                async.complete();
            }
        });
    }

    public String getURL(String path) {
        return getURL(path, 0);
    }

    public String getURL(String path, int version) {
        return getURL(DEFAULT_METHOD, version, path);
    }

    public String getURL(String method, int version, String path) {
        StringBuilder builder = new StringBuilder();
        if(method != null && !method.isEmpty()) {
            builder.append(method).append(":").append("/");
        }
        if(version > 0) {
            if(builder.lastIndexOf("/") != (builder.length() - 1)) {
                builder.append("/");
            }
            builder.append("v").append(version).append("/");
        }
        if(path != null && !path.isEmpty()) {
            if(builder.lastIndexOf("/") != (builder.length() - 1)) {
                builder.append("/");
            }
            builder.append(path);
        }

        return builder.toString();
    }

    @APIHandler()
    public static class TestEndpointHandlerWithoutBaseURL extends SeamlessHandler {

        @Endpoint(path = VERSION_AND_PATH, version = 1)
        public String versionAndPath() {
            return VERSION_AND_PATH;
        }

        @Endpoint(path = NO_VERSION_AND_PATH)
        public String noVersionAndPath() {
            return NO_VERSION_AND_PATH;
        }

        @Endpoint(version = 1)
        public String versionAndNoPath() {
            return VERSION_AND_NO_PATH;
        }

        @Endpoint()
        public String noVersionAndNoPath() {
            return NO_VERSION_AND_NO_PATH;
        }

    }

    private SeamlessResponse getResponse(Message message) {
        return Json.decodeValue(message.body().toString(), SeamlessResponse.class);
    }

}
