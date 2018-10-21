package com.appstetix.appstract.seamless.core.generic;

import com.appstetix.appstract.seamless.core.api.SeamlessAPILayer;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.APIViolationException;

public class ValidatorProcessor {

    APIValidator[] validators;

    public ValidatorProcessor(APIValidator[] validators) {
        this.validators = validators;
    }

    public void process(SeamlessRequest request, SeamlessAPILayer apiLayer) throws APIViolationException {
        if(validators != null && validators.length > 0) {
            for(APIValidator validator : this.validators) {
                validator.validate(request, apiLayer);
            }
        }
    }
}
