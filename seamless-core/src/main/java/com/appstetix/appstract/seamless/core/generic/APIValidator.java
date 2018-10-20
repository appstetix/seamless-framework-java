package com.appstetix.appstract.seamless.core.generic;

public interface APIValidator {
    void validate(SeamlessRequest request) throws RuntimeException;
}
