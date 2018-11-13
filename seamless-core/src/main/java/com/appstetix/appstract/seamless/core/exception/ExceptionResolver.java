package com.appstetix.appstract.seamless.core.exception;

import com.appstetix.appstract.seamless.core.annotation.APIException;
import com.appstetix.appstract.seamless.core.annotation.EnableExceptionHandling;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.core.exception.custom.ExceptionResolverException;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;
import com.appstetix.appstract.seamless.core.exception.handler.DefaultExceptionHandler;
import com.appstetix.appstract.seamless.core.util.AnnotationUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ExceptionResolver {

    private static ExceptionResolver instance = null;
    private Map<String, ExceptionHandler> handlers;
    private ExceptionHandler defaultHandler = new DefaultExceptionHandler();

    private ExceptionResolver(EnableExceptionHandling handlers) throws ExceptionResolverException {
        try {
            if(handlers != null) {
                this.handlers = new HashMap();
                if(handlers.exceptions().length > 0) {
                    for(Class<? extends ExceptionHandler> exception : handlers.exceptions()) {
                        setupException(exception);
                    }
                } else {
                    log.info("No exception handlers listed explicitly. Scanning project for exception handlers");
                    final Set<String> exceptions = AnnotationUtil.findClassNamesWithAnnotation(APIException.class);
                    log.info("after scanning for exception handlers");
                    if(exceptions != null && !exceptions.isEmpty()) {
                        for (String exception : exceptions) {
                            log.info("creating exception handler [{}]", exception);
                            Class<? extends ExceptionHandler> exceptionClass = (Class<? extends ExceptionHandler>) Class.forName(exception);
                            setupException(exceptionClass);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionResolverException(e);
        }
    }

    public static ExceptionResolver getInstance(EnableExceptionHandling handlers) throws ExceptionResolverException {
        if(instance == null) {
            instance = new ExceptionResolver(handlers);
        }
        return instance;
    }

    public SeamlessResponse resolve(SeamlessRequest request, String errorClass, Throwable exception) {
        if(exception == null) {
            throw new IllegalArgumentException("parameter [exception] cannot be null or empty");
        }
        ExceptionHandler handler = this.defaultHandler;
        if(handlers != null && !handlers.isEmpty() && handlers.containsKey(errorClass)) {
            handler = handlers.get(errorClass);
        }
        return composeResponse(handler, request, exception);
    }

    private SeamlessResponse composeResponse(ExceptionHandler handler, SeamlessRequest request, Throwable exception) {
        final SeamlessResponse response = SeamlessResponse.builder()
                .code(handler.responseCode(request, exception))
                .headers(handler.headers(request, exception))
                .payload(handler.body(request, exception)).build();

        return response;
    }

    private void setupException(Class<? extends ExceptionHandler> exception) throws InstantiationException, IllegalAccessException {
        final APIException customException = exception.getDeclaredAnnotation(APIException.class);
        this.handlers.put(customException.value().getName(), exception.newInstance());
    }

}
