package com.appstetix.appstract.seamless.aws.factory;

import com.appstetix.appstract.seamless.core.generic.SeamlessRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
@AllArgsConstructor
public abstract class AWSEventHandler {

    private AWSEventHandler successor;
    private String identifyingKey;

    public AWSEventHandler(String identifyingKey) {
        this.identifyingKey = identifyingKey;
    }

    public SeamlessRequest getRequest(Map<String, Object> input) throws Exception {
        if(input.containsKey(identifyingKey)) {
            return handler(input);
        }
        return successor != null ? successor.getRequest(input) : null;
    }

    protected abstract SeamlessRequest handler(Map<String, Object> input) throws Exception;

}
