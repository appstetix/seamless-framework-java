package com.appstetix.appstract.seamless.core.api;

public interface SeamlessProvider<REQ, RESP> {

    SeamlessRequest convertRequest(REQ request);
    RESP convertResponse(SeamlessResponse response);

}

