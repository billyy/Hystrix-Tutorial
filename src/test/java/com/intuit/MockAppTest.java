package com.intuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.netflix.config.DynamicPropertyFactory;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import rx.Observable;
import rx.observers.TestSubscriber;


/**
 * Unit test for simple App.
 */
public class MockAppTest {
	private static final String weatherResponse = "{\"coord\":{\"lon\":-122.09,\"lat\":37.39},\"sys\":{\"message\":0.0126,\"country\":\"US\",\"sunrise\":1430744953,\"sunset\":1430794857},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"Sky is Clear\",\"icon\":\"01d\"}],\"base\":\"stations\",\"main\":{\"temp\":292.866,\"temp_min\":292.866,\"temp_max\":292.866,\"pressure\":988.1,\"sea_level\":1026.22,\"grnd_level\":988.1,\"humidity\":70},\"wind\":{\"speed\":1.38,\"deg\":243},\"clouds\":{\"all\":0},\"dt\":1430777146,\"id\":0,\"name\":\"Mountain View\",\"cod\":200}";
	private static final String intuResponse = "// [ { \"c\": \"+2.90\", \"t\" : \"INTU\"}] ";	
	private static final String aaplResponse = "// [ { \"c\": \"+0.30\", \"t\" : \"AAPL\"}] ";	
	private static final String yhooResponse = "// [ { \"c\": \"-0.74\", \"t\" : \"YHOO\"}] ";	
	private static final String googResponse = "// [ { \"c\": \"-0.40\", \"t\" : \"GOOG\"}] ";	
	private static final String cscoResponse = "// [ { \"c\": \"-0.74\", \"t\" : \"CSCO\"}] ";	

    private static String[] symbols = {"INTU", "YHOO", "AAPL", "GOOG", "CSCO"};
    
    private HystrixRequestContext context = null;
    

    @BeforeClass
    public static void setup() {   	
       	
       //	HystrixPlugins.reset();

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
    	config.addProperty("com.intuit.external.stock.host", "localhost");
    	config.addProperty("com.intuit.external.stock.port", "8080");
        
    }   

    @Before
    public void init() {
 


    	
        context = HystrixRequestContext.initializeContext();
        RequestScopeObject.set("testWireMock");
        
        stubFor(get(urlEqualTo("/data/2.5/weather?zip=94040,us"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                   .withFixedDelay(500)
                    .withBody(weatherResponse)));
        
        stubFor(get(urlEqualTo("/finance/info?client=ig&q=INTU"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                   .withFixedDelay(500)
                    .withBody(intuResponse)));
        
        stubFor(get(urlEqualTo("/finance/info?client=ig&q=AAPL"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withFixedDelay(500)
                    .withBody(aaplResponse)));
        
        stubFor(get(urlEqualTo("/finance/info?client=ig&q=YHOO"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withFixedDelay(500)
                    .withBody(yhooResponse)));
        stubFor(get(urlEqualTo("/finance/info?client=ig&q=GOOG"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withFixedDelay(500)
                    .withBody(googResponse)));
        
        stubFor(get(urlEqualTo("/finance/info?client=ig&q=CSCO"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(200)
                    .withFixedDelay(500)
                    .withBody(cscoResponse)));

    }

   
    
    @After
    public void tearDown() {
    	WireMock.shutdownServer();
    	context.shutdown();
     }
    
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    

    @Test
    public void testSync()
    {
     	
    	final long startTime = System.currentTimeMillis();
    	//Get humidity
    	WeatherCommand wCommand = new WeatherCommand("94040");
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
 
    }
 
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
		if (wMap.get() != null) {  //block on the weather call
			for(int inx = 0; inx < 5; inx++) {   //block on the order of stock calls.   
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
    
    /**
     * This code works, but not efficient.  It requires to subscribe() (and block) twice to process observables.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Ignore
    @Test
    public void testLessEfficentObservable() throws InterruptedException, ExecutionException
    {
 		final long startTime = System.currentTimeMillis();
 		final Map<String, Double> weather = new HashMap<String, Double>();
    	TestSubscriber<Object> ts = new TestSubscriber<Object>();    	
    	TestSubscriber<Object> ts2 = new TestSubscriber<Object>();    	

 		WeatherCommand wCommand = new WeatherCommand("94040");
    	Observable<Map<String, Double>> w = wCommand.observe();    	
 
    	//Compose a single observable from the five stock observable.  
      	Observable<Map<String, String>> stocks = (new StockCommand(symbols[0])).observe();       		 
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[1])).observe());
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[2])).observe());
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[3])).observe());
   		stocks = Observable.merge(stocks, (new StockCommand(symbols[4])).observe());
   	
   		//Subscribe and block on the weather observable.
   		w.doOnNext((m) -> weather.put("humidity", m.get("humidity"))).subscribe(ts);
    	ts.awaitTerminalEvent();    	
    
    	//Subscribe and block on the stock observable.
    	stocks
    	.map(Marshaller::transform)  
    	.doOnNext( (data) -> System.out.println(data.get("symbol") + " is Correlated = " + Compute.affect(weather.get("humidity"), data.get("change").toString())))
    	.subscribe(ts2);
    	
    	ts2.awaitTerminalEvent();
        
        System.out.println("Less efficient Observable Duration = " + (System.currentTimeMillis() - startTime));        


    }
    
    /**
     * Block only once and most efficient.   Each stock call is processed as soon as it returns.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testObservable() throws InterruptedException, ExecutionException
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
   					  System.out.println("complete");
   					  latch.countDown();
   				  });
   	  		
   		latch.await();
           
        System.out.println("Observable Duration = " + (System.currentTimeMillis() - startTime));        


    }    
    
}
