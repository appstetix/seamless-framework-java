package com.appstetix.appstract.seamless.core.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.CONTENT_TYPE;
import static com.appstetix.appstract.seamless.core.generic.HttpHeaders.Value.*;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeamlessResponse {

    public static final int DEFAULT_ERROR_CODE = -1;

    private int code;
    private Map<String, String> headers;
    private Object payload;
    private Throwable error;
    private String errorClass;

    public SeamlessResponse(int code, Map<String, String> headers) {
        this(code, headers, null);
    }

    public SeamlessResponse(int code, Object payload) {
        this(code, null, payload);
    }

    public SeamlessResponse(int code, Map<String, String> headers, Object payload) {
        this.code = code;
        this.headers = headers;
        this.payload = payload;
        determineContentType();
    }

    public SeamlessResponse(Throwable error) {
        this(error, null);
    }

    public SeamlessResponse(Throwable error, Object payload) {
        this.error = error;
        this.errorClass = error.getClass().getName();
        this.payload = payload;
        this.code = DEFAULT_ERROR_CODE;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    public boolean isError() {
        return code >= 300 && code < 600;
    }

    public boolean hasError() {
        return this.error != null;
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
        return getHeader(CONTENT_TYPE);
    }

    protected void determineContentType() {
        if(!hasHeaders()) {
            this.headers = new HashMap();
        }
        if(!this.headers.containsKey(CONTENT_TYPE)) {
            if(hasPayload()) {
                if (payload instanceof String) {
                    if(((String)payload).startsWith("<")) {
                        this.headers.put(CONTENT_TYPE, TEXT_HTML);
                    }
                } else if(payload instanceof byte[]) {
                    this.headers.put(CONTENT_TYPE, APPLICATION_OCTET_STREAM);
                } else {
                    this.headers.put(CONTENT_TYPE, APPLICATION_JSON);
                }
            } else {
                this.headers.put(CONTENT_TYPE, TEXT_PLAIN);
            }
        }

    }
}
