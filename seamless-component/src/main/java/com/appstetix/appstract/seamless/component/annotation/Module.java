package com.appstetix.appstract.seamless.component.annotation;

import com.appstetix.appstract.seamless.component.SeamlessComponent;

public @interface Module {
    Class<? extends SeamlessComponent> processor();
}
