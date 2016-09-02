package com.intuit;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
//import com.netflix.hystrix.exception.HystrixTimeoutException;

import java.io.IOException;



public final class WeatherCommand extends HystrixCommand<Map<String, Double>> {
	private final static String QUERY_FORMAT = "/data/2.5/weather?zip=%s,us";;
	private final String query;
    public WeatherCommand(String zip) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("WeatherGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        //.withExecutionTimeoutInMilliseconds(5000)
                        ));
        
          
        query = String.format(QUERY_FORMAT, zip);
    }

    @SuppressWarnings({ "unchecked", "deprecation", "resource" })
	@Override
    protected Map<String, Double> run() throws ClientProtocolException, IOException //, HystrixTimeoutException 
    {
    	System.out.println("Request Scope Object = " + RequestScopeObject.get());
    	
    	
		Map<String, Object> retMap = null;
		  
	      // specify the host, protocol, and port
		String url = DynamicPropertyFactory.getInstance()
			    .getStringProperty("com.intuit.external.weather.host", "api.openweathermap.org")
			    .get();
		Integer port = DynamicPropertyFactory.getInstance()
			    .getIntProperty("com.intuit.external.weather.port", 80)
			    .get();

		  System.out.println(url + ":" + port);
	      HttpHost target = new HttpHost(url, port, "http");
	       
		
	       
	      // specify the get request
	      HttpGet getRequest = new HttpGet(query);	 
 	 
	      try {
	      	final HttpParams httpParams = new BasicHttpParams();
	    	HttpConnectionParams.setConnectionTimeout(httpParams, 1);
	    	HttpClient httpClient = new DefaultHttpClient(httpParams);
	        HttpResponse httpResponse = httpClient.execute(target, getRequest);
	        HttpEntity entity = httpResponse.getEntity();
	      	      
		      if (entity != null) {
		        String jsonString = EntityUtils.toString(entity);
		        retMap = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
		      }
 
	      } catch(ConnectTimeoutException ex) {
	    	  //So Hystrix will count this one as time-out, not failure.
	    	 // throw new HystrixTimeoutException();
	      }
	    
	    return (Map<String, Double>)retMap.get("main");
    
    
    }
    
   
}
