package com.appstetix.appstract.seamless.core.generic;

import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.SeamlessFilterException;

public interface APIFilter<T> {
    void filter(SeamlessRequest request, T rawInput) throws SeamlessFilterException;
}
