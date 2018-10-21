package com.appstetix.appstract.seamless.core.generic;

import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.APIViolationException;

public interface APIValidator {
    void validate(SeamlessRequest request) throws APIViolationException;
}
