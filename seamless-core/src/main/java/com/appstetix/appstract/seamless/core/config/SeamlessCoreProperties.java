package com.appstetix.appstract.seamless.core.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.appstetix.appstract.seamless.component.EnvVarUtil.StringValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeamlessCoreProperties {

    public static final String APPLICATION_NAME = StringValue("seamless.app.name", "Seamless Application");

}
