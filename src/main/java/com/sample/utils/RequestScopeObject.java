package com.sample.utils;

/**
 * Demo class for using TLS with Hystrix.
 * @author byuen
 *
 */
public class RequestScopeObject {

    private static ThreadLocal<String> value = new ThreadLocal<String>();

    public static String get() {
        return value.get();
    }

    public static void set(String name) {
        value.set(name);
    }
}
