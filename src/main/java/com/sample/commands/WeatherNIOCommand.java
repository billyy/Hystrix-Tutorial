package com.sample.commands; 

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientBuilder;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.client.RxClient.ClientConfig.Builder;


public class WeatherNIOCommand extends HystrixObservableCommand<Map<String, Double>> {

	private final static String QUERY_FORMAT = "/data/2.5/weather?zip=%s,us";;
	private final String query;
	private final static Gson gson = new Gson();

    public WeatherNIOCommand(String zip) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("WeatherGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("WeatherNIOCommand"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(10)
                        .withExecutionTimeoutInMilliseconds(2000))); 
        query = String.format(QUERY_FORMAT, zip);
    }

    @SuppressWarnings("unchecked")
	@Override
    protected rx.Observable<Map<String, Double>> construct() {
		String host = DynamicPropertyFactory.getInstance()
			    .getStringProperty("com.intuit.external.weather.host", "api.openweathermap.org")
			    .get();
		Integer port = DynamicPropertyFactory.getInstance()
			    .getIntProperty("com.intuit.external.weather.port", 80)
			    .get();

        int timeoutMillis = 2000;
        RxClient.ClientConfig clientConfig = new Builder(null)
                .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS).build();
        HttpClient<ByteBuf, ByteBuf> client = new HttpClientBuilder<ByteBuf, ByteBuf>(host, port)
                .config(clientConfig).build();
             
        return client
                .submit(HttpClientRequest.createGet(query))
                .flatMap((HttpClientResponse<ByteBuf> r) -> 
                r.getContent().map(b -> 
                (Map<String, Double>)((Map<String, Object>)gson.fromJson(convert(new ByteBufInputStream(b)), new TypeToken<HashMap<String, Object>>() {}.getType())).get("main")  
                ));
    }

    

    private static String convert(InputStream inputStream) {
    	@SuppressWarnings("resource")
		Scanner s = new Scanner(inputStream).useDelimiter("\\A");
    	String result = s.hasNext() ? s.next() : "";
    	
    	return result;
    }
}