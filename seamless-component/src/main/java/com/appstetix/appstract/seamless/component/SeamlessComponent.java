package com.appstetix.appstract.seamless.component;

import com.appstetix.appstract.seamless.component.exception.ModuleInstallationException;

public interface SeamlessComponent {
    void install(Class clazz) throws ModuleInstallationException;
}
