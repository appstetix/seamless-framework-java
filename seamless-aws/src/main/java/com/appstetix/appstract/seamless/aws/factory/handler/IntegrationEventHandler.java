package com.appstetix.appstract.seamless.aws.factory.handler;

import com.appstetix.appstract.seamless.aws.factory.AWSEventHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import io.vertx.core.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class IntegrationEventHandler extends AWSEventHandler {

    private static final String EVENT_SOURCE = "eventsource";

    private static final String AWS_SNS = "aws:sns";
    private static final String AWS_KINESIS = "aws:kinesis";

    public IntegrationEventHandler(String identifyingKey) {
        super(identifyingKey);
    }

    @Override
    public SeamlessRequest handler(Map<String, Object> input) throws Exception {

        final List records = (List) input.get(getIdentifyingKey());
        if(records != null && !records.isEmpty()) {

            final Map<String, Object> event = (Map) records.get(0);
            final Optional<String> option = event.keySet().stream().filter(o -> o.toLowerCase().equals(EVENT_SOURCE)).findFirst();
            if(option.isPresent()) {

                String source = (String) event.get(option.get());

                switch (source) {
                    case AWS_SNS: return createSNSRequest(event);
                    case AWS_KINESIS: return createKinesisRequest(event);
                    default: {
                        String errorMessage = String.format("Unable to identify event source [%s]", source);
                        log.error(errorMessage);
                        throw new Exception(errorMessage);
                    }
                }
            }
            throw new Exception(String.format("Unable to determine source from event: %s", event));

        } else {
            String errorMessage = String.format("No records found for input: %s", input);
            log.error(errorMessage);
            throw new Exception(errorMessage);
        }
    }

    private SeamlessRequest createSNSRequest(Map<String, Object> event) throws Exception {
        final SeamlessRequest request = new SeamlessRequest();

        final Map<String, Object> sns = (Map) event.get(SnsKey.SNS);
        request.setPath(getPathFromSource((String) sns.get(SnsKey.ARN)));
        //set body
        request.setBody((String) sns.get(SnsKey.MESSAGE));

        //set parameters
        sns.forEach((key, value) -> {
            if(!SnsKey.MESSAGE.equals(key.toString())) {
                request.addParameter((String) key, value);
            }
        });

        Map<String, String> headers = new HashMap();

        //set headers
        event.forEach((key, value) -> {
            if(!SnsKey.SNS.equals(key)) {
                headers.put(key, String.valueOf(value));
            }
        });
        request.setHeaders(headers);

        return request;
    }

    private SeamlessRequest createKinesisRequest(Map<String, Object> event) throws Exception {
        final SeamlessRequest request = new SeamlessRequest();
        request.setPath(getPathFromSource((String) event.get(KinesisKey.ARN)));

        final Map kinesis = (Map) event.get(KinesisKey.KINESIS);

        //set body
        String base64Data = (String) kinesis.get(KinesisKey.DATA);
        request.setBody(new String(Base64.getDecoder().decode(base64Data)));

        //set parameters
        kinesis.forEach((key, value) -> {
            if(!KinesisKey.DATA.equals(key.toString())) {
                request.addParameter((String) key, value);
            }
        });

        Map<String, String> headers = new HashMap();

        //set headers
        event.forEach((key, value) -> {
            if(!KinesisKey.KINESIS.equals(key)) {
                headers.put(key, String.valueOf(value));
            }
        });
        request.setHeaders(headers);

        return request;
    }

    private String getPathFromSource(String arn) {
        if(arn != null && !arn.isEmpty()) {
            final String[] parts = arn.split(":");
            return parts[parts.length - 1];
        }
        return null;
    }

    public class SnsKey {
        public static final String SNS = "Sns";
        public static final String MESSAGE = "Message";
        public static final String ARN = "TopicArn";
    }

    public class KinesisKey {
        public static final String KINESIS = "kinesis";
        public static final String DATA = "data";
        public static final String ARN = "eventSourceARN";
    }
}
