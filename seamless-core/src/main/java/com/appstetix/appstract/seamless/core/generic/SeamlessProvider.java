package com.appstetix.appstract.seamless.core.generic;

public interface SeamlessProvider<REQ, RESP> {

    SeamlessRequest convertRequest(REQ request);
    RESP convertResponse(SeamlessResponse response);

}

