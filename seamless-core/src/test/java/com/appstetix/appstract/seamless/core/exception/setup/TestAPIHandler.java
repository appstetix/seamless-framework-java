package com.appstetix.appstract.seamless.core.exception.setup;

import com.appstetix.appstract.seamless.core.annotation.APIHandler;
import com.appstetix.appstract.seamless.core.annotation.Endpoint;
import com.appstetix.appstract.seamless.core.api.SeamlessHandler;

@APIHandler(baseURL = "ex")
public class TestAPIHandler extends SeamlessHandler {

    public static final String CUSTOM_EXCEPTION_MESSAGE = "Some Custom Exception Message";
    public static final String DYNAMIC_EXCEPTION_MESSAGE = "Some Dynamic Exception Message";

    @Endpoint(path = "default")
    public void throwDefaultException() throws Exception {
        throw new Exception();
    }

    @Endpoint(path = "custom")
    public void throwCustomException() throws Exception {
        throw new CustomException(CUSTOM_EXCEPTION_MESSAGE);
    }

    @Endpoint(path = "dynamic")
    public void throwDynamicException() throws Exception {
        throw new DynamicException(DYNAMIC_EXCEPTION_MESSAGE);
    }

}
