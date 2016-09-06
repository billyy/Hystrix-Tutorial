package com.sample.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

import java.io.IOException;



public final class StockCommand extends HystrixCommand<Map<String, String>> {
	private final String QUERY_FORMAT = "/finance/info?client=ig&q=%s";
	private final String query;
    public StockCommand(String symbol) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("StockGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        ));
         query = String.format(QUERY_FORMAT, symbol);
    }

    @Override
    protected Map<String, String> run() throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClientBuilder.create().build();
		Map<String, String> retMap = null;
		  
	      // specify the host, protocol, and port
		String url = DynamicPropertyFactory.getInstance()
			    .getStringProperty("com.intuit.external.stock.host", "finance.google.com")
			    .get();
		Integer port = DynamicPropertyFactory.getInstance()
			    .getIntProperty("com.intuit.external.stock.port", 80)
			    .get();

	    HttpHost target = new HttpHost(url, port, "http");
	       
	    // specify the get request
	    HttpGet getRequest = new HttpGet(query);	 
 	 
	    HttpResponse httpResponse = httpclient.execute(target, getRequest);
	    HttpEntity entity = httpResponse.getEntity();
	     	 
	    if (entity != null) {
	        String jsonString = EntityUtils.toString(entity).substring(3);
	        Gson gson = new GsonBuilder().create();
	        ArrayList<Map<String, String>> myList = gson.fromJson(jsonString,
	                new TypeToken<ArrayList<HashMap<String, String>>>() {
	                }.getType());

	        retMap = myList.size() > 0 ? myList.get(0) : null;
	    }
 	    
	    return retMap;    
    }
  
}
