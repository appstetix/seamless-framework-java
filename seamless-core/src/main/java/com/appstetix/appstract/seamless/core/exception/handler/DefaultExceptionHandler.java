package com.appstetix.appstract.seamless.core.exception.handler;

import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;
import com.appstetix.appstract.seamless.core.util.ExceptionUtil;
import io.vertx.core.eventbus.ReplyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class DefaultExceptionHandler implements ExceptionHandler<String> {

    public static final String DEFAULT_ERROR_MESSAGE = "Unable to process your request at this time";
    public static final String NO_RESOURCE_FOUND_ERROR_MESSAGE_PATTERN = "No resource for '%s' was found";

    @Override
    public int responseCode(SeamlessRequest request, Throwable exception) {
        return isReplyException(exception) ? 404 : 500;
    }

    @Override
    public Map<String, String> headers(SeamlessRequest request, Throwable exception) {
        return null;
    }

    @Override
    public String body(SeamlessRequest request, Throwable exception) {
        if(isReplyException(exception)) {
            return String.format(NO_RESOURCE_FOUND_ERROR_MESSAGE_PATTERN, request.getPath());
        }
        log.error(ExceptionUtil.getStackTraceString(exception));
        if(StringUtils.isNotEmpty(exception.getMessage()) || "null".equalsIgnoreCase(exception.getMessage())) {
            return exception.getMessage();
        }
        return DEFAULT_ERROR_MESSAGE;
    }

    private boolean isReplyException(Throwable exception) {
        return ReplyException.class.getName().equals(exception.getClass().getName());
    }
}
