package com.appstetix.appstract.seamless.core.generic;

import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.APIFilterException;

import java.util.LinkedHashSet;
import java.util.Set;

public class FilterProcessor {

    private Set<APIFilter> filters;

    public FilterProcessor(Class<? extends APIFilter>[] filters) throws IllegalAccessException, InstantiationException {
        if(filters != null && filters.length > 0) {
            this.filters = new LinkedHashSet();
            for (Class filter: filters) {
                this.filters.add((APIFilter) filter.newInstance());
            }
        }
    }

    public void begin(SeamlessRequest request, Object rawInput) throws APIFilterException {
        if(this.filters != null && !filters.isEmpty()) {
            for (APIFilter filter: this.filters) {
                filter.handle(request, rawInput);
            }
        }
    }

}
