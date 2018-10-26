package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.annotation.API;
import com.appstetix.appstract.seamless.core.annotation.APIHandler;
import com.appstetix.appstract.seamless.core.annotation.Endpoint;
import com.appstetix.appstract.seamless.core.annotation.Task;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class APITest {

    private static final String SUCCESSFULLY_EXECUTED_ENDPOINT = "Successfully executed endpoint";
    private static final String SUCCESSFULLY_EXECUTED_TASK = "Successfully executed task";
    private static final String TEST_TASK_NAME = "OnTestTask";
    private static final String DEFAULT_URL = "GET:/api/";

    private static EventBus eb;

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        eb = SeamlessAPI.vertx.eventBus();
        SeamlessAPI.vertx.deployVerticle(TestHandler1.class.getName());
//        new TestAPI();
    }

    @Test
    public void testAPIEndpoint(TestContext context) {
        final Async async = context.async();
        eb.send(DEFAULT_URL, null, messageAsyncResult -> {
            try {
                context.assertTrue(messageAsyncResult.succeeded());
                context.assertEquals(SUCCESSFULLY_EXECUTED_ENDPOINT, messageAsyncResult.result().body());
            } catch(Exception ex) {
                context.fail();
            }  finally {
                async.complete();
            }
        });
    }

    @Ignore
    public void testAPITask(TestContext context) {
        final Async async = context.async();
        eb.send(TEST_TASK_NAME, null, messageAsyncResult -> {
            try {
                context.assertTrue(messageAsyncResult.succeeded());
                context.assertEquals(SUCCESSFULLY_EXECUTED_TASK, messageAsyncResult.result().body());
            } catch(Exception ex) {
                context.fail();
            }  finally {
                async.complete();
            }
        });
    }

    @API(handlers = TestHandler1.class )
    public static class TestAPI extends SeamlessAPI {
        @Override
        public SeamlessRequest convertRequest(Object request) {
            return null;
        }

        @Override
        public Object convertResponse(SeamlessResponse response) {
            return null;
        }
    }

    @APIHandler(baseURL = "api")
    public static class TestHandler1 extends SeamlessHandler {

        @Endpoint()
        public void method1(Message message) {
            message.reply(SUCCESSFULLY_EXECUTED_ENDPOINT);
        }

        @Task(TEST_TASK_NAME)
        public void testTask(Message message) {
            message.reply(SUCCESSFULLY_EXECUTED_TASK);
        }

    }

}
