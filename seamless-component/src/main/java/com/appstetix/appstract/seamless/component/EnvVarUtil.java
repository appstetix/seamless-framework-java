package com.appstetix.appstract.seamless.component;

import org.apache.commons.lang3.StringUtils;

public class EnvVarUtil {

    public static final String StringValue(String property) {
        return StringValue(property, null);
    }

    public static final String StringValue(String property, String def) {
        return getSystemValue(property, def);
    }

    public static final boolean BooleanValue(String property, String def) {
        return Boolean.parseBoolean(getSystemValue(property, def));
    }

    public static final int IntegerValue(String property, String def) {
        return Integer.parseInt(getSystemValue(property, def));
    }

    private static final String getSystemValue(String property, String def) {
        if(StringUtils.isEmpty(property)) {
            throw new IllegalArgumentException("parameter [property] cannot be null or empty");
        }
        String delimiter = System.getenv("delimiter");

        if(StringUtils.isNotEmpty(delimiter)) {
            property = property.replaceAll("\\.", delimiter);
        }

        String propertyValue = System.getenv(property);

        if(StringUtils.isEmpty(propertyValue)) {
            propertyValue = System.getProperty(property);
        }

        if(StringUtils.isEmpty(propertyValue)) {
            if(StringUtils.isNotEmpty(def)) {
                return def;
            }
            throw new IllegalArgumentException(String.format("property [%s] must be set in the environment variables", property));
        }
        return propertyValue;
    }


}
