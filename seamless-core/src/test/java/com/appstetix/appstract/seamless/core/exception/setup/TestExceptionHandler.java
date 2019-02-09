package com.appstetix.appstract.seamless.core.exception.setup;

import com.appstetix.appstract.seamless.core.annotation.APIException;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;

import java.util.Map;

@APIException(CustomException.class)
@APIException(DynamicException.class)
public class TestExceptionHandler implements ExceptionHandler<String> {

    public static final int EXCEPTION_ERROR_CODE = 512;

    @Override
    public int responseCode(SeamlessRequest request, Throwable exception) {
        return EXCEPTION_ERROR_CODE;
    }

    @Override
    public Map<String, String> headers(SeamlessRequest request, Throwable exception) {
        return null;
    }

    @Override
    public String body(SeamlessRequest request, Throwable exception) {
        return exception.getMessage();
    }


}

