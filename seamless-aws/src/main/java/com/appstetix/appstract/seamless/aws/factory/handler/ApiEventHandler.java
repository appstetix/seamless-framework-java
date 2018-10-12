package com.appstetix.appstract.seamless.aws.factory.handler;

import com.appstetix.appstract.seamless.aws.factory.AWSEventHandler;
import com.appstetix.appstract.seamless.core.generic.SeamlessRequest;

import java.util.ArrayList;
import java.util.Map;

public class ApiEventHandler extends AWSEventHandler {

    public ApiEventHandler(String identifyingKey) {
        super(identifyingKey);
    }

    @Override
    public SeamlessRequest handler(Map<String, Object> input) throws Exception {
        final SeamlessRequest request = new SeamlessRequest();

        final Map queryStringParameters = (Map) input.get("queryStringParameters");
        if(queryStringParameters != null && !queryStringParameters.isEmpty()) {
            queryStringParameters.forEach((key, vals) -> {
                if(vals.getClass().isArray()) {
                    ArrayList values = (ArrayList) vals;
                    values.forEach(o -> {
                        request.addParameter((String) key, o);
                    });
                } else {
                    request.addParameter((String) key, vals);
                }
            });
        }

        request.setHeaders((Map<String, String>) input.get("headers"));
        request.setMethod((String) input.get("httpMethod"));
        request.setPath((String) input.get("path"));
        request.setBody((String) input.get("body"));

        return request;
    }
}
