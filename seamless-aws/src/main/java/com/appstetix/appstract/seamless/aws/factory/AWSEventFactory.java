package com.appstetix.appstract.seamless.aws.factory;

import com.appstetix.appstract.seamless.aws.factory.handler.ApiEventHandler;
import com.appstetix.appstract.seamless.aws.factory.handler.IntegrationEventHandler;
import com.appstetix.appstract.seamless.aws.factory.handler.ScheduledEventHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;

import java.util.Map;

public final class AWSEventFactory {

    private static AWSEventFactory instance = new AWSEventFactory();
    private AWSEventHandler entrypoint;

    private AWSEventFactory() {

        ApiEventHandler apiEventHandler = new ApiEventHandler("path");
        IntegrationEventHandler integrationEventHandler = new IntegrationEventHandler("Records");
        ScheduledEventHandler scheduledEventHandler = new ScheduledEventHandler("source");

        apiEventHandler.setSuccessor(integrationEventHandler);
        integrationEventHandler.setSuccessor(scheduledEventHandler);

        this.entrypoint = apiEventHandler;
    }

    public static AWSEventFactory getInstance() {
        return instance;
    }

    public SeamlessRequest createRequest(Map<String, Object> event) throws Exception {
        if(event == null || event.isEmpty()) {
            throw new IllegalArgumentException("parameter [event] cannot be null");
        }
        return entrypoint.getRequest(event);
    }
}
