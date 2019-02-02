package com.appstetix.appstract.seamless.core.validator;

import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;

public interface APIValidator {
    void validate(SeamlessRequest request, SeamlessAPI apiLayer) throws Exception;
}
