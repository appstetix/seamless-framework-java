package com.appstetix.appstract.seamless.aws.factory;

import com.appstetix.appstract.seamless.core.generic.SeamlessRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class AWSEventFactoryTest {

    public static final String API_GATEWAY_EVENT_FILE = "api_gateway_event.json";
    public static final String SNS_EVENT_FILE = "sns_event.json";
    public static final String KINESIS_EVENT_FILE = "kinesis_event.json";
    public static final String SCHEDULED_EVENT_FILE = "scheduled_event.json";

    @Test
    public void unknownEventTest() {
        final Map<String, Object> event = new HashMap();
        event.put("test", "some property");
        try {
            final SeamlessRequest request = AWSEventFactory.getInstance().createRequest(event);
            Assert.assertNull(request);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void apiGatewayEventTest() {
        try {
            final Map<String, Object> event = createAPIGatewayTestEvent();
            Assert.assertNotNull(event);
            Assert.assertFalse(event.isEmpty());

            final SeamlessRequest request = AWSEventFactory.getInstance().createRequest(event);
            Assert.assertNotNull(request);
            Assert.assertNotNull(request.getPath());
            Assert.assertEquals(request.getPath(), "/test/hello");
            Assert.assertNotNull(request.getMethod());
            Assert.assertEquals(request.getMethod(), "GET");
            Assert.assertEquals(request.getRequestPath(), "GET:/test/hello");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void snsEventTest() {
        try {
            final Map<String, Object> event = createSNSTestEvent();
            Assert.assertNotNull(event);
            Assert.assertFalse(event.isEmpty());

            final SeamlessRequest request = AWSEventFactory.getInstance().createRequest(event);
            Assert.assertNotNull(request);
            Assert.assertNotNull(request.getPath());
            Assert.assertEquals(request.getPath(), "OnTestTopic");
            Assert.assertNotNull(request.getMethod());
            Assert.assertEquals(request.getMethod(), "PATCH");
            Assert.assertEquals(request.getRequestPath(), "PATCH:OnTestTopic");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void kinesisEventTest() {
        try {
            final Map<String, Object> event = createKinesisTestEvent();
            Assert.assertNotNull(event);
            Assert.assertFalse(event.isEmpty());

            final SeamlessRequest request = AWSEventFactory.getInstance().createRequest(event);
            Assert.assertNotNull(request);
            Assert.assertNotNull(request.getPath());
            Assert.assertEquals(request.getPath(), "OnTestStream");
            Assert.assertNotNull(request.getMethod());
            Assert.assertEquals(request.getMethod(), "PATCH");
            Assert.assertEquals(request.getRequestPath(), "PATCH:OnTestStream");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void scheduledEventTest() {
        try {
            final Map<String, Object> event = createScheduledTestEvent();
            Assert.assertNotNull(event);
            Assert.assertFalse(event.isEmpty());

            final SeamlessRequest request = AWSEventFactory.getInstance().createRequest(event);
            Assert.assertNotNull(request);
            Assert.assertNotNull(request.getPath());
            Assert.assertEquals(request.getPath(), "rule/my-schedule");
            Assert.assertNotNull(request.getMethod());
            Assert.assertEquals(request.getMethod(), "PATCH");
            Assert.assertEquals(request.getRequestPath(), "PATCH:rule/my-schedule");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private Map<String, Object> createAPIGatewayTestEvent() throws IOException {
        return getEventProperties(API_GATEWAY_EVENT_FILE);
    }

    private Map<String, Object> createSNSTestEvent() throws IOException {
        return getEventProperties(SNS_EVENT_FILE);
    }

    private Map<String, Object> createKinesisTestEvent() throws IOException {
        return getEventProperties(KINESIS_EVENT_FILE);
    }

    private Map<String, Object> createScheduledTestEvent() throws IOException {
        return getEventProperties(SCHEDULED_EVENT_FILE);
    }

    private Map<String, Object> getEventProperties(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getResource(filename), Map.class);
    }

    private InputStream getResource(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream(resourceName);
    }

}
