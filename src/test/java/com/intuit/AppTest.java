package com.intuit;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.util.Asserts;
import org.junit.Ignore;
import org.junit.Test;




import com.netflix.hystrix.exception.HystrixRuntimeException;

import rx.Observable;
import rx.observers.TestSubscriber;


/**
 * These tests will call the REAL services and the service might fail.
 */
public class AppTest {

    private static String[] symbols = {"INTU", "YHOO", "AAPL", "GOOG", "CSCO"};
 
    
    @Test
    public void testSync()
    {
   
//    	ConfigurationManager.getConfigInstance().setProperty("hystrix.command.WeatherCommand.circuitBreaker.forceOpen", "true");     	
    	try {
	    	final long startTime = System.currentTimeMillis();
	    	//Get humidity
	    	WeatherCommand wCommand = new WeatherCommand("94040");
	    	//HystrixProperty<Boolean> forceClosed = wCommand.getProperties().circuitBreakerForceClosed();
	    	Map<String, Double> wMap = wCommand.execute();
	        Double humidity = wMap.get("humidity");
	
	    	for (int inx = 0; inx < 5; inx++) {
	 	        StockCommand iCommand = new StockCommand(symbols[inx]);
		        Map<String, String> iMap = iCommand.execute();
		        //transform the data for print
		        Map<String, Object> data = new HashMap<>();
		        data.put("symbol", symbols[inx]);
		        data.put("change", iMap.get("c"));  //Get Stock Change 
		        data.put("humidity", humidity);
		        //execute print
		        Utility.print(data);
	    	}
	        System.out.println("Sync Duration = " + (System.currentTimeMillis() - startTime));      
    	} catch(HystrixRuntimeException ex) {
    		
    		System.out.println("Exception = " + ex.getMessage());
    	}
 
    }
 
    @Ignore
    @Test
    public void testAsync() throws InterruptedException, ExecutionException
    {

		final long startTime = System.currentTimeMillis();

    	WeatherCommand wCommand = new WeatherCommand("94040");
    	Future<Map<String, Double>> wMap = wCommand.queue();

    	List<Future<Map<String, String>>> list = new ArrayList<Future<Map<String, String>>>();
		for (int inx = 0; inx < 5; inx++) {
	        StockCommand iCommand = new StockCommand(symbols[inx]);
	        list.add(iCommand.queue());
     
 		}
		//Get humidity
		if (wMap.get() != null) {
			for(int inx = 0; inx < 5; inx++) {
		        //transform the data for print
		        Map<String, Object> data = new HashMap<>();
		        data.put("symbol", symbols[inx]);
		        data.put("change", list.get(inx).get().get("c"));  //Get Stock Change 
		        data.put("humidity", wMap.get().get("humidity"));
		        //execute print
		        Utility.print(data);
			}
		}
        System.out.println("Async Duration = " + (System.currentTimeMillis() - startTime));        

    }
    
    
    @Ignore
    @Test
    public void testObservable() throws InterruptedException, ExecutionException
    {
 
 		final long startTime = System.currentTimeMillis();
 		final Map<String, Double> weather = new HashMap<String, Double>();
    	TestSubscriber<Object> ts = new TestSubscriber<Object>();    	
    	TestSubscriber<Object> ts2 = new TestSubscriber<Object>();    	

 		WeatherCommand wCommand = new WeatherCommand("94040");
    	Observable<Map<String, Double>> w = wCommand.observe();    	
 
      	Observable<Map<String, String>> stocks = (new StockCommand(symbols[0])).observe();       		 
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[1])).observe());
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[2])).observe());
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[3])).observe());
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[4])).observe());
   	
   		
   		w.doOnNext((m) -> weather.put("humidity", m.get("humidity"))).subscribe(ts);
    	ts.awaitTerminalEvent();    	
    	
    	stocks
    	.map(Marshaller::transform)  
    	.doOnNext( (data) -> System.out.println(data.get("symbol") + " is Correlated = " + Compute.affect(weather.get("humidity"), data.get("change").toString())))
    	.subscribe(ts2);
    	
    	ts2.awaitTerminalEvent();
        
        System.out.println("Observable Duration = " + (System.currentTimeMillis() - startTime));        


    }
    
    @Ignore
    @Test
    public void testObservable2() throws InterruptedException, ExecutionException
    {

 		final long startTime = System.currentTimeMillis();
    	final CountDownLatch latch = new CountDownLatch(1);
      	Observable<Map<String, String>> temp = (new StockCommand(symbols[0])).observe();       		 
   		temp = Observable.merge(temp, (new StockCommand(symbols[1])).observe());
   		temp = Observable.merge(temp, (new StockCommand(symbols[2])).observe());
   		temp = Observable.merge(temp, (new StockCommand(symbols[3])).observe());
   		final Observable<Map<String, String>> stocks = Observable.merge(temp, (new StockCommand(symbols[4])).observe());
   		
 		WeatherCommand wCommand = new WeatherCommand("94040");
    	Observable<Map<String, Double>> w = wCommand.observe();   	
   		
   		w.flatMap((m) -> {
   					
					final Double humidity = m.get("humidity");
			   		//Combine stock and weather and transform the data
   					return stocks.map((data)-> {
   				   		Map<String, Object> map = new HashMap<String, Object>();
   				   		map.put("symbol", data.get("t"));
   				   		map.put("change", data.get("c"));
   				   		map.put("humidity", humidity);
   				   		return map;  						
   					}).asObservable();
    			
   		})
   		.subscribe(Utility::print, 
   				  (error)-> { 
   					  System.out.println(error); 
   					  latch.countDown(); 					  
   				  }, 
   				  ()-> {
   					  //System.out.println("complete");
   					  latch.countDown();
   				  });
   		
   		w.toBlocking();
   		latch.await();
           
        System.out.println("Observable Duration = " + (System.currentTimeMillis() - startTime));        


    }
    
}
