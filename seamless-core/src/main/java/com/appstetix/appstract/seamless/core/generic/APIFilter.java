package com.appstetix.appstract.seamless.core.generic;

import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.SeamlessFilterException;

public abstract class APIFilter<T> {

    public void process(SeamlessRequest request, T rawInput) {}

    public abstract boolean handle(SeamlessRequest request, T rawInput) throws SeamlessFilterException;

}
