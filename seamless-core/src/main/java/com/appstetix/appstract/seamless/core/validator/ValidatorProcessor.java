package com.appstetix.appstract.seamless.core.validator;

import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;

public class ValidatorProcessor {

    APIValidator[] validators;

    public ValidatorProcessor(APIValidator[] validators) {
        this.validators = validators;
    }

    public void process(SeamlessRequest request, SeamlessAPI apiLayer) throws Exception {
        if(validators != null && validators.length > 0) {
            for(APIValidator validator : this.validators) {
                validator.validate(request, apiLayer);
            }
        }
    }
}
