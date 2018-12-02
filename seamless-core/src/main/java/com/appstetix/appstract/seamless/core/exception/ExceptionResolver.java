package com.appstetix.appstract.seamless.core.exception;

import com.appstetix.appstract.seamless.core.annotation.APIException;
import com.appstetix.appstract.seamless.core.annotation.EnableExceptionHandling;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.core.exception.custom.ExceptionResolverException;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionContainer;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;
import com.appstetix.appstract.seamless.core.exception.handler.DefaultExceptionHandler;
import com.appstetix.appstract.seamless.core.util.AnnotationUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ExceptionResolver {

    private static ExceptionResolver instance = null;
    private Map<String, ExceptionHandler> handlers;
    private ExceptionHandler defaultHandler;

    private ExceptionResolver(EnableExceptionHandling handlers) throws ExceptionResolverException {
        try {
            if(handlers != null) {
                this.handlers = new HashMap();
                Set<Class<? extends ExceptionHandler>> exceptionHandlers = new LinkedHashSet();
                if(hasDefaultExceptionsClass(handlers) || handlers.exceptions().length > 0) {
                    if(hasDefaultExceptionsClass(handlers)) {
                        final ExceptionContainer exceptionContainer = handlers.exceptionsClass().newInstance();
                        exceptionHandlers.addAll(Arrays.asList(exceptionContainer.getHandlerClasses()));
                    }
                    if(handlers.exceptions().length > 0) {
                        exceptionHandlers.addAll(Arrays.asList(handlers.exceptions()));
                    }
                } else {
                    log.info("No exception handlers listed explicitly. Scanning project for exception handlers");
                    final Set<String> exceptions = AnnotationUtil.findClassNamesWithAnnotation(APIException.class);
                    if(exceptions != null && !exceptions.isEmpty()) {
                        for (String exception : exceptions) {
                            Class<? extends ExceptionHandler> exceptionClass = (Class<? extends ExceptionHandler>) Class.forName(exception);
                            exceptionHandlers.add(exceptionClass);
                        }
                    }
                }
                for(Class<? extends ExceptionHandler> exception : exceptionHandlers) {
                    setupException(exception);
                }
                this.defaultHandler = handlers.defaultHandler().newInstance();
            } else {
                this.defaultHandler = new DefaultExceptionHandler();
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
        log.info("creating exception handler [{}]", exception);
        final APIException customException = exception.getDeclaredAnnotation(APIException.class);
        this.handlers.put(customException.value().getName(), exception.newInstance());
    }

    private boolean hasDefaultExceptionsClass(EnableExceptionHandling handlers) {
        return !handlers.exceptionsClass().getName().equals(EnableExceptionHandling.DefaultExceptionsClass.class.getName());
    }

}
