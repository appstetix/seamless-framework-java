package com.appstetix.appstract.seamless.core.generic;

public class HttpHeaders {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";
    public static final String USER_AGENT = "User-Agent";
    public static final String REFERER = "Referer";

    public class Value {
        public static final String APPLICATION_JSON = "application/json";
        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_PLAIN = "text/plain";
        public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    }

    public class ResponseCode {
        public static final int SUCCESSFUL = 200;
        public static final int ACCEPTED = 202;
        public static final int CREATED = 201;
        public static final int NO_CONTENT = 204;
        public static final int BAD_REQUEST_ERROR = 400;
        public static final int UNAUTHORIZED_ERROR = 401;
        public static final int CONFLICT_ERROR = 409;
        public static final int SERVER_ERROR = 500;
    }

}
