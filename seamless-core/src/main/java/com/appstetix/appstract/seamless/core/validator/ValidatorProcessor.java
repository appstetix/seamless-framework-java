package com.appstetix.appstract.seamless.core.validator;

import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ValidatorProcessor {

    private List<APIValidator> validators;

    public ValidatorProcessor(Class<? extends APIValidator>[] validatorClasses) throws IllegalAccessException, InstantiationException {
        if(validatorClasses != null) {
            this.validators = new LinkedList();
            for(Class<? extends APIValidator> validator : validatorClasses) {
                log.info(String.format("INSTALLING VALIDATOR: [%s]", validator));
                validators.add(validator.newInstance());
            }
        }
    }

    public void process(SeamlessRequest request, SeamlessAPI apiLayer) throws Exception {
        if(validators != null && !validators.isEmpty()) {
            for(APIValidator validator : this.validators) {
                validator.validate(request, apiLayer);
            }
        }
    }
}
