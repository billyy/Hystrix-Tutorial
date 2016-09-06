package com.sample.callable;


import java.util.concurrent.Callable;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;


public class SampleHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

	@Override
	public <K> Callable<K> wrapCallable(Callable<K> callable) {
		return new SampleHystrixContextCallable<K>(callable); 
	}
}
