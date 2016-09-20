package com.sample;


import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/*
 * 1.   Semaphore setting will compare the actual execution time vs the timeout setting to determine whether throwing Runtime exception or not.  That behavior is by design.
 * 2.   With semaphore,  it will NOT interrupt the thread to stop execution.  If the thread is set to sleep forever, it will just block forever and not throw the Runtime exception.   
 */

public class HystrixTimeoutTest {

    @Test(expected = HystrixRuntimeException.class)
    public void testTimeout() {
        final Thread currentThread = Thread.currentThread();
        HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("groupKey"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionTimeoutEnabled(true)
                        .withExecutionTimeoutInMilliseconds(1000)
                );
        new HystrixCommand<Void>(setter) {
            @Override
            protected Void run() throws Exception {
                // SEMAPHORE mode means same thread execution
                assertEquals(Thread.currentThread(), currentThread);

                // Pause to allow Hystrix to timeout
                Thread.sleep(2000);
                return null;
            }
        }.execute();

        // we don't get here
        fail();
    }
}

