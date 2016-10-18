package com.sample.callable;


import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

import com.sample.utils.RequestScopeObject;


public class SampleHystrixContextCallable<K> implements Callable<K> {

    private Map<String, String> parentMDC;
    private String value = null;

    private final Callable<K> actual;

    public SampleHystrixContextCallable(Callable<K> actual) {
        this.actual = actual;
        this.parentMDC = MDC.getCopyOfContextMap();
        this.value = RequestScopeObject.get();
    }

    @Override
    public K call() throws Exception {
        RequestScopeObject.set(value);

        Map<String, String> childMDC = MDC.getCopyOfContextMap();
        try {
            if (parentMDC != null)
                MDC.setContextMap(parentMDC);
            return actual.call();
        } finally {
            if (childMDC != null)
                MDC.setContextMap(childMDC);
        }
    }
}
