package com.appstetix.appstract.seamless.core.generic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeamlessResponse {

    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private int code;
    private Map<String, String> headers;
    private Object payload;
    private String errorMessage;

    public SeamlessResponse(int code, Map<String, String> headers) {
        this.code = code;
        this.headers = headers;
    }

    public SeamlessResponse(int code, Object payload) {
        this.code = code;
        this.payload = payload;
    }

    public SeamlessResponse(int code, Map<String, String> headers, Object payload) {
        this.code = code;
        this.headers = headers;
        this.payload = payload;
        determineContentType();
    }

    public SeamlessResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    public boolean isError() {
        return code >= 300 && code < 600;
    }

    public boolean hasErrorMessage() {
        return StringUtils.isNotEmpty(this.errorMessage);
    }

    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
        determineContentType();
    }

    public String getHeader(String key) {
        if(hasHeaders() && StringUtils.isNotEmpty(key)) {
            return this.headers.get(key);
        }
        return null;
    }

    public boolean hasPayload() {
        return this.payload != null;
    }

    public void setPayload(Object data) {
        this.payload = data;
        determineContentType();
    }

    public String getContentType() {
        return getHeader(CONTENT_TYPE_HEADER);
    }

    protected void determineContentType() {
        if(!hasHeaders()) {
            this.headers = new HashMap();
        }
        if(!this.headers.containsKey(CONTENT_TYPE_HEADER)) {
            if(hasPayload()) {
                if (payload instanceof String) {
                    if(((String)payload).startsWith("<")) {
                        this.headers.put(CONTENT_TYPE_HEADER, TEXT_HTML);
                    }
                } else if(payload instanceof byte[]) {
                    this.headers.put(CONTENT_TYPE_HEADER, APPLICATION_OCTET_STREAM);
                } else {
                    this.headers.put(CONTENT_TYPE_HEADER, APPLICATION_JSON);
                }
            } else {
                this.headers.put(CONTENT_TYPE_HEADER, TEXT_PLAIN);
            }
        }

    }
}
