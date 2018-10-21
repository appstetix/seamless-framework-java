package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.exception.IllegalParameterFormatException;
import com.appstetix.appstract.seamless.core.generic.UserContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeamlessRequest {

    private static final String REQUEST_PATH_PATTERN = "%s:%s";

    private String path;
    private Map<String, String> headers;
    private Map<String, Object> parameters;
    private UserContext userContext;
    private String method;
    private String body;

    public String getRequestPath() {
        if(StringUtils.isEmpty(method)) {
            return path.trim();
        }
        return String.format(REQUEST_PATH_PATTERN, method.trim().toUpperCase(), path.trim()).trim();
    }

    public void addParameter(String key, Object value) {
        if(this.parameters == null) {
            this.parameters = new HashMap();
        }
        if(hasParameter(key)) {
            final Object parameter = getParameter(key);
            if(parameter instanceof String[]) {
                String[] array = (String[]) parameter;
                array[array.length] = (String) value;
                this.parameters.replace(key, array);
            } else {
                String[] array = new String[]{};
                array[0] = String.valueOf(parameter);
                array[0] = String.valueOf(value);
                this.parameters.put(key, array);
            }
        } else {
            this.parameters.put(key, value);
        }
    }

    public boolean hasParameter(String key) {
        if(this.parameters != null) {
            return this.parameters.containsKey(key);
        }
        return false;
    }

    public Object getParameter(String key) {
        if(this.parameters != null) {
            return this.parameters.get(key);
        }
        return null;
    }

    public <T> T getParameter(String key, Class<T> clazz) {
        final Object parameter = getParameter(key);
        return Objects.isNull(parameter) ? null : clazz.cast(parameter);
    }

    public String getParameterAsString(String key) {
        return getParameterAsString(key, null);
    }

    public String getParameterAsString(String key, String def) {
        if(this.parameters != null) {
            final Object value = this.parameters.get(key);
            if(!Objects.isNull(value)) {
                return String.valueOf(value);
            }
        }
        return def;
    }

    public Integer getParameterAsInteger(String key) {
        return getParameterAsInteger(key, null);
    }

    public Integer getParameterAsInteger(String key, Integer def) throws IllegalParameterFormatException {
        Object value = null;
        try {
            if(this.hasParameter(key)) {
                value = this.parameters.get(key);
                if(!Objects.isNull(value)) {
                    if(value.getClass().isArray()) {
                        String[] arr = (String[]) value;
                        System.out.println("Found array...returning last value found. index " + (arr.length - 1));
                        value = arr[arr.length - 1];
                    }
                    return Integer.parseInt(String.valueOf(value));
                }
            }
            return def;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new IllegalParameterFormatException(key, "Integer", value);
        }
    }

    public Boolean getParameterAsBoolean(String key) {
        return getParameterAsBoolean(key, null);
    }

    public Boolean getParameterAsBoolean(String key, Boolean def) throws IllegalParameterFormatException {
        Object value = null;
        try {
            if(this.hasParameter(key)) {
                value = this.parameters.get(key);
                if(!Objects.isNull(value)) {
                    if (value.getClass().isArray()) {
                        String[] arr = (String[]) value;
                        value = arr[arr.length - 1];
                    }
                }
                return Boolean.parseBoolean(String.valueOf(value));
            }
            return def;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new IllegalParameterFormatException(key, "Boolean", value);
        }
    }

    public Long getParameterAsLong(String key) {
        return getParameterAsLong(key, null);
    }

    public Long getParameterAsLong(String key, Long def) throws IllegalParameterFormatException {
        Object value = null;
        try {
            if(this.hasParameter(key)) {
                value = this.parameters.get(key);
                if(!Objects.isNull(value)) {
                    if (value.getClass().isArray()) {
                        String[] arr = (String[]) value;
                        value = arr[arr.length - 1];
                    }
                }
                return Long.parseLong(String.valueOf(value));
            }
            return def;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new IllegalParameterFormatException(key, "Long", value);
        }
    }

    public Double getParameterAsDouble(String key) {
        return getParameterAsDouble(key, null);
    }

    public Double getParameterAsDouble(String key, Double def) throws IllegalParameterFormatException {
        Object value = null;
        try {
            if(this.hasParameter(key)) {
                value = this.parameters.get(key);
                if(!Objects.isNull(value)) {
                    if (value.getClass().isArray()) {
                        String[] arr = (String[]) value;
                        value = arr[arr.length - 1];
                    }
                }
                return Double.parseDouble(String.valueOf(value));
            }
            return def;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new IllegalParameterFormatException(key, "Double", value);
        }
    }

    public String[] getParameterAsArray(String key) {
        return getParameterAsArray(key, null);
    }

    public String[] getParameterAsArray(String key, String[] def) {
        if(this.hasParameter(key)) {
            final Object value = this.getParameter(key);
            if(!Objects.isNull(value)) {
                if (value.getClass().isArray()) {
                    return (String[]) value;
                }
            }
            return new String[]{String.valueOf(value)};
        }
        return def;
    }

    public boolean hasHeader(String key) {
        if(this.headers != null) {
            return this.headers.containsKey(key);
        }
        return false;
    }

    public String getHeader(String key) {
        if(hasHeader(key)) {
            return this.headers.get(key);
        }
        return null;
    }

    public Integer getHeaderAsInteger(String key, int def) {
        if(hasHeader(key)) {
            final String value = this.getHeader(key);
            if(StringUtils.isNotEmpty(value)) {
                return Integer.parseInt(value);
            }
        }
        return def;
    }

    public String getUserProfileId() {
        if(userContext != null) {
            return userContext.getAsString("uuid");
        }
        return null;
    }
}
