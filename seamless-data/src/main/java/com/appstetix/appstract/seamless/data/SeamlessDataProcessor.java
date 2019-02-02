package com.appstetix.appstract.seamless.data;

import com.appstetix.appstract.seamless.component.SeamlessComponent;
import com.appstetix.appstract.seamless.component.exception.ModuleInstallationException;
import com.appstetix.appstract.seamless.data.annotation.Assets;
import org.codejargon.feather.Feather;

public class SeamlessDataProcessor implements SeamlessComponent {

    @Override
    public void install(Class clazz) throws ModuleInstallationException {
        if(clazz != null) {
            if(clazz.isAnnotationPresent(Assets.class)) {
                Assets assets = (Assets) clazz.getDeclaredAnnotation(Assets.class);
                if(assets.primary() != null) {
                    Feather.with(assets.primary()).injectFields(clazz);
                } else {
                    Feather.with().injectFields(clazz);
                }
            }
        }
    }
}
