package com.appstetix.appstract.seamless.component.annotation;

import java.lang.annotation.*;

@Retention( RetentionPolicy.RUNTIME )
public @interface Instructions {

    Instruction[] value();

    @Repeatable(value = Instructions.class)
    public @interface Instruction {
        String key();
        String data();
    }
}
