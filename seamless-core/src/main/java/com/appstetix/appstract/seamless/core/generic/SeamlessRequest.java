package com.appstetix.appstract.seamless.core.generic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeamlessRequest {

    private static final String REQUEST_PATH_PATTERN = "%s:%s";

    private String path;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private UserContext userContext;
    private String method;
    private String body;

    public boolean isValid() {
        return true;
    }

    public String getRequestPath() {
        return String.format(REQUEST_PATH_PATTERN, method.trim().toUpperCase(), path.trim()).trim();
    }

    public void addParameter(String key, String value) {
        if(this.parameters == null) {
            this.parameters = new HashMap();
        }
        this.parameters.put(key, value);
    }

    public boolean hasParameter(String key) {
        if(this.parameters != null) {
            return this.parameters.containsKey(key);
        }
        return false;
    }

    public String getParameter(String key) {
        if(this.parameters != null) {
            return this.parameters.get(key);
        }
        return null;
    }

    public Integer getParameterAsInteger(String key) {
        if(this.hasParameter(key)) {
            return Integer.parseInt(this.parameters.get(key));
        }
        return null;
    }

    public Boolean getParameterAsBoolean(String key) {
        if(this.hasParameter(key)) {
            return Boolean.parseBoolean(this.parameters.get(key));
        }
        return null;
    }

    public Long getParameterAsLong(String key) {
        if(this.hasParameter(key)) {
            return Long.parseLong(this.parameters.get(key));
        }
        return null;
    }

    public Double getParameterAsDouble(String key) {
        if(this.hasParameter(key)) {
            return Double.parseDouble(this.parameters.get(key));
        }
        return null;
    }

    public boolean hasHeader(String key) {
        if(this.headers != null) {
            return this.headers.containsKey(key);
        }
        return false;
    }

    public String getHeader(String key) {
        if(this.headers != null) {
            return this.headers.get(key);
        }
        return null;
    }

    public Integer getHeaderAsInteger(String key) {
        if(hasHeader(key)) {
            return Integer.parseInt(this.headers.get(key));
        }
        return null;
    }

    public String getUserProfileId() {
        if(userContext != null) {
            return userContext.getAsString("uuid");
        }
        return null;
    }
}
