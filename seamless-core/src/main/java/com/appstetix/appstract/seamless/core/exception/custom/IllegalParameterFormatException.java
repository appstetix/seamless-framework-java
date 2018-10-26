package com.appstetix.appstract.seamless.core.exception.custom;

public class IllegalParameterFormatException extends ClassCastException {

    public static final String ILLEGAL_PARAMETER_FORMAT_EXCEPTION_PATTER = "parameter [%s] expected a %s but found %s";

    public IllegalParameterFormatException(String parameter, String targetType, Object value) {
        super(String.format(ILLEGAL_PARAMETER_FORMAT_EXCEPTION_PATTER, parameter, targetType, String.valueOf(value)));
    }

    public IllegalParameterFormatException(String s) {
        super(s);
    }

}
