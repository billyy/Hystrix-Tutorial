package com.sample;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.netflix.config.DynamicPropertyFactory;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.sample.callable.SampleHystrixConcurrencyStrategy;
import com.sample.commands.WeatherCommand;
import com.sample.commands.WeatherNIOCommand;
import com.sample.logging.LoggingHelper;
import com.sample.utils.RequestScopeObject;

import rx.Observable;
import rx.Subscriber;


/**
 * Unit test for simple App.
 */
public class MockWeatherTest {
	private static final String weatherResponse = "{\"coord\":{\"lon\":-122.09,\"lat\":37.39},\"sys\":{\"message\":0.0126,\"country\":\"US\",\"sunrise\":1430744953,\"sunset\":1430794857},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"Sky is Clear\",\"icon\":\"01d\"}],\"base\":\"stations\",\"main\":{\"temp\":292.866,\"temp_min\":292.866,\"temp_max\":292.866,\"pressure\":988.1,\"sea_level\":1026.22,\"grnd_level\":988.1,\"humidity\":70},\"wind\":{\"speed\":1.38,\"deg\":243},\"clouds\":{\"all\":0},\"dt\":1430777146,\"id\":0,\"name\":\"Mountain View\",\"cod\":200}";
    
    private HystrixRequestContext context = null;
    

    @BeforeClass
    public static void setup() {   	
       	
      	HystrixPlugins.getInstance().registerConcurrencyStrategy(
				(new SampleHystrixConcurrencyStrategy()));
    	
       	AbstractConfiguration config = new AbstractConfiguration() {

    		private Map<String, Object> map = new HashMap<>();
    		
			@Override
			public boolean containsKey(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Iterator<String> getKeys() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getProperty(String arg0) {
				// TODO Auto-generated method stub
				return map.get(arg0);
			}

			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			protected void addPropertyDirect(String arg0, Object arg1) {
				map.put(arg0, arg1);
				
			}
    	};
			
			
    	DynamicPropertyFactory.initWithConfigurationSource(config);
    	
    	config.addProperty("com.intuit.external.weather.host", "localhost");
    	config.addProperty("com.intuit.external.weather.port", "8080");
        
    }   

    @Before
    public void init() {
    	
        context = HystrixRequestContext.initializeContext();
       
        stubFor(get(urlEqualTo("/data/2.5/weather?zip=94040,us"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                   .withFixedDelay(1000)
                    .withBody(weatherResponse)));
    }

   
    
    @After
    public void tearDown() {
    	
    	LoggingHelper.log();
    	
    	WireMock.shutdownServer();
    	RequestScopeObject.set(null);
    	context.shutdown();
     }
    
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);


    @Test
    public void testWeatherCommandSync()
    {    	
    	final long startTime = System.currentTimeMillis();
    	//Get humidity
    	WeatherCommand wCommand = new WeatherCommand("94040");
    	Map<String, Double> map = wCommand.execute();
        System.out.println("Sync Duration = " + (System.currentTimeMillis() - startTime));  
        map.forEach((k,v)->System.out.println("Key : " + k + " Value : " + v));

     }
    
    @Test
    public void testWeatherCommandASync() throws InterruptedException, ExecutionException
    {    	
    	final long startTime = System.currentTimeMillis();
    	//Get humidity
    	WeatherCommand wCommand = new WeatherCommand("94040");
    	Future<Map<String, Double>> f = wCommand.queue();
        System.out.println("Async Duration = " + (System.currentTimeMillis() - startTime));    

        Map<String, Double> map = f.get();
        map.forEach((k,v)->System.out.println("Key : " + k + " Value : " + v));
	    
  		System.out.println("Async Complete Duration = " + (System.currentTimeMillis() - startTime));    		
       
    }
    
    @Test
    public void testWeatherCommandReactive() throws InterruptedException
    {    	
    	System.out.println(Thread.currentThread().getName());
        String tid = UUID.randomUUID().toString();
		System.out.println("Tracking id = " + tid);
		RequestScopeObject.set(tid);
    	
    	final long startTime = System.currentTimeMillis();
    
    	WeatherCommand wCommand = new WeatherCommand("94040");
    	Observable<Map<String, Double>> o = wCommand.observe();
        System.out.println("Reactive Duration = " + (System.currentTimeMillis() - startTime));    
        CountDownLatch latch = new CountDownLatch(1);
        
 			o.subscribe(new MySubscriber(startTime, latch));
 			 			
 			latch.await();
    
    }
    
    @Test
    public void testWeatherCommandNIO() throws InterruptedException {
        String tid = UUID.randomUUID().toString();
        System.out.println(Thread.currentThread().getName());
 		System.out.println("Tracking id = " + tid);
 		RequestScopeObject.set(tid);
     	
     	final long startTime = System.currentTimeMillis();
     
     	WeatherNIOCommand wCommand = new WeatherNIOCommand("94040");
     	Observable<Map<String, Double>> o = wCommand.observe();
         System.out.println("NIO Duration = " + (System.currentTimeMillis() - startTime));    
         CountDownLatch latch = new CountDownLatch(1);
         
         o.subscribe(new MySubscriber(startTime, latch));
  			 			
  		 latch.await();
    } 
    
    private static class MySubscriber extends Subscriber<Map<String, Double>> {
    		private final long startTime;
    		private final CountDownLatch latch;
    		
    		public MySubscriber(long startTime, CountDownLatch latch) {
    			this.latch = latch;
    			this.startTime = startTime;
    		}
    		
			@Override
			public void onCompleted() {				
		        System.out.println("Reactive Complete Duration = " + (System.currentTimeMillis() - startTime));    			
		        latch.countDown();
			}

			@Override
			public void onError(Throwable arg0) {
		        System.out.println("Error Duration = " + (System.currentTimeMillis() - startTime));    			
		        System.out.println(arg0.getMessage());    			
		        latch.countDown();
				
			}

			@Override
			public void onNext(Map<String, Double> arg0) {
			     System.out.println("OnNext Duration = " + (System.currentTimeMillis() - startTime));    
				 System.out.println(Thread.currentThread().getName());
		         arg0.forEach((k,v)->System.out.println("Key : " + k + " Value : " + v));
			}
    }
 
}
