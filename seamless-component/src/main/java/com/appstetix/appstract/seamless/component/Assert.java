package com.appstetix.appstract.seamless.component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assert {

    public static void notNull(Object obj, String errorMessage) throws IllegalArgumentException {
        if(obj == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void notNullOrEmpty(Collection collection, String errorMessage) throws IllegalArgumentException {
        notNull(collection, errorMessage);
        if(collection.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void notNullOrEmpty(String value, String errorMessage) throws IllegalArgumentException {
        notNull(value, errorMessage);
        if(value.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void isNotEmpty(Collection collection, String errorMessage) {
        if(collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static boolean isNotEmpty(Map map, String errorMessage) {
        if(map == null || map.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return true;
    }

    public static boolean isEmpty(Collection collection) {
        if(collection != null) {
            return collection.isEmpty();
        }
        return true;
    }

    public static boolean isEmpty(Map map) {
        if(map != null) {
            return map.isEmpty();
        }
        return true;
    }
}
