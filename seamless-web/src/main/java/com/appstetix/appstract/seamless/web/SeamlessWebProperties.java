package com.appstetix.appstract.seamless.web;

import com.appstetix.appstract.seamless.component.EnvVarUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeamlessWebProperties {

    public static final int APPLICATION_PORT = EnvVarUtil.IntegerValue("seamless.web.server.port", "8888");

}
