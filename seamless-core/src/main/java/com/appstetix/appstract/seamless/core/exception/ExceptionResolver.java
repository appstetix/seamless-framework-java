package com.appstetix.appstract.seamless.core.exception;

import com.appstetix.appstract.seamless.core.annotation.APIException;
import com.appstetix.appstract.seamless.core.annotation.EnableExceptionHandling;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.core.exception.custom.ExceptionResolverException;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;
import com.appstetix.appstract.seamless.core.exception.handler.DefaultExceptionHandler;
import com.appstetix.appstract.seamless.core.util.AnnotationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExceptionResolver {

    private static ExceptionResolver instance = null;
    private Map<String, ExceptionHandler> handlers;
    private ExceptionHandler defaultHandler = new DefaultExceptionHandler();

    private ExceptionResolver(EnableExceptionHandling handlers) throws ExceptionResolverException {
        try {
            final Set<String> exceptions = AnnotationUtil.findClassNamesWithAnnotation(APIException.class);
            if(exceptions != null && !exceptions.isEmpty()) {
                this.handlers = new HashMap();
                for (String exception : exceptions) {
                    Class exceptionClass = Class.forName(exception);
                    final APIException customException = (APIException) exceptionClass.getDeclaredAnnotation(APIException.class);
                    this.handlers.put(customException.value().getName(), (ExceptionHandler) exceptionClass.newInstance());
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
        if(request == null) {
            throw new IllegalArgumentException("parameter [request] cannot be null or empty");
        }
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
        return new SeamlessResponse(handler.responseCode(request, exception),
                                        handler.headers(request, exception),
                                            handler.body(request, exception));
    }

}
