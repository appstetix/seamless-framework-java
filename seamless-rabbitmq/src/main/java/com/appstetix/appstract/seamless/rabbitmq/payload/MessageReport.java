package com.appstetix.appstract.seamless.rabbitmq.payload;

import lombok.Data;

import java.util.Map;

@Data
public class MessageReport {

    private String queue;
    private Map<String, String> headers;
    private Object body;

}
