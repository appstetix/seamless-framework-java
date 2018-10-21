package com.appstetix.appstract.seamless.aws.factory.handler;

import com.appstetix.appstract.seamless.aws.factory.AWSEventHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import io.vertx.core.http.HttpMethod;

import java.util.List;
import java.util.Map;

public class ScheduledEventHandler extends AWSEventHandler {

    public ScheduledEventHandler(String identifyingKey) {
        super(identifyingKey);
    }

    @Override
    public SeamlessRequest handler(Map<String, Object> input) throws Exception {
        final SeamlessRequest request = new SeamlessRequest();
        request.setMethod(HttpMethod.PATCH.toString());
        request.setPath(getPathFromEvent(input));

        request.setBody(String.valueOf(input.get("detail")));

        input.forEach((key, value) -> {
            if("detail".equals(key)) {
                request.addParameter(key, String.valueOf(value));
            }
        });

        return request;
    }

    private String getPathFromEvent(Map<String, Object> input) {
        List<String> resources = (List) input.get("resources");
        final String[] parts = resources.get(0).split(":");
        return parts[parts.length - 1];
    }

}
