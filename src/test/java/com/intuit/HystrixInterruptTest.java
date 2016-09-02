package com.intuit;

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;

public class HystrixInterruptTest {

	@Test	
	public void testInterrupt() {
	       long t1 = System.currentTimeMillis();
	        for (int i = 0; i < 5; i++) {
	            try{
	               new CallGoogle().execute();
	            } catch(Exception ex) {
	                System.out.println(ex);
	            }
	            long t2 = System.currentTimeMillis();
	            System.out.println("Time(ms) : ---->" + (t2 - t1));
	        }
	}
	
	private static class CallGoogle extends HystrixCommand<String> {
		private HttpClient httpClient = null;

		public CallGoogle() {
			
		       super(
		                Setter.withGroupKey(
		                        HystrixCommandGroupKey.Factory
		                                .asKey("play"))
		                        .andThreadPoolKey(
		                                HystrixThreadPoolKey.Factory
		                                        .asKey("play_group"))
		                        .andCommandPropertiesDefaults(
		                                HystrixCommandProperties
		                                        .Setter()
		                                        .withExecutionIsolationThreadTimeoutInMilliseconds(
		                                                1)));		
		       
			HttpParams httpParams = new BasicHttpParams();
			httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.TRUE);
			httpParams.setParameter(CoreProtocolPNames.USER_AGENT, "ABC");

			HttpConnectionParams.setStaleCheckingEnabled(httpParams, Boolean.TRUE);

			//SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			//schemeRegistry.register(new Scheme("https", 443, sf));

			//Initialize the http connection pooling
			PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);

			// Initialize the connection parameters for performance tuning
			connectionManager.setMaxTotal(12);
			connectionManager.setDefaultMaxPerRoute(10);

			httpClient = new DefaultHttpClient(connectionManager, httpParams);
			
		}

		@Override
		protected String run() throws Exception {
			HttpGet get = new HttpGet("http://www.google.co.in");
	        HttpResponse response = httpClient.execute(get);
	        System.out.println("Response:" + response);
	        
	        return null;
		}
	}
}
