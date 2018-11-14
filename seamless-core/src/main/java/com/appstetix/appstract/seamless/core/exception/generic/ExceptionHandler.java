package com.appstetix.appstract.seamless.core.exception.generic;

import com.appstetix.appstract.seamless.core.api.SeamlessRequest;

import java.util.Map;

public interface ExceptionHandler<T> {
    int responseCode(SeamlessRequest request, Throwable exception);
    Map<String, String> headers(SeamlessRequest request, Throwable exception);
    T body(SeamlessRequest request, Throwable exception);
}
