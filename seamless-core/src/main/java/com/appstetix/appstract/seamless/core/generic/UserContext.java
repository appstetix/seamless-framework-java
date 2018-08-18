package com.appstetix.appstract.seamless.core.generic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContext {

    @Getter
    @Setter
    private Map<String, Object> ctx;

    public UserContext(Map<String, Object> contextData) {
        this.ctx = contextData;
    }

    public boolean hasContext() {
        return ctx != null && !ctx.isEmpty();
    }

    public String getAsString(String key) {
        if(hasContext()) {
            return (String) ctx.get(key);
        }
        return null;
    }

    public Integer getAsInteger(String key) {
        if(hasContext()) {
            return (int) ctx.get(key);
        }
        return null;
    }

    public Double getAsDouble(String key) {
        if(hasContext()) {
            return (Double) ctx.get(key);
        }
        return null;
    }

    public Boolean getAsBoolean(String key) {
        if(hasContext()) {
            return (Boolean) ctx.get(key);
        }
        return null;
    }

}
