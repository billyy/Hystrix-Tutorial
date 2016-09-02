package com.intuit.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;






import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

public class LoggingFilter implements Filter {
	private final static Logger Log = LoggerFactory.getLogger(LoggingFilter.class);
	private final static String HYSTRIX_CMP_ID = "unique_app_id";  //PLEASE CHANGE!!!!
    private final static String HYSTRIX_EXCEPTION_STACK_LOG_ENABLED = "";   //PLEASE CHANGE!!!!
    
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        //Create Hystrix Context only if one has not been created.		
		HystrixRequestContext context = HystrixRequestContext.initializeContext();
		String companyId = null; //ADD CODE TO RETRIEVE COMPANY ID
		try {
			HttpServletRequest request = (HttpServletRequest) req;
			@SuppressWarnings("unchecked")
			Enumeration<String> tid = request.getHeaders("intuitTid");
			String intuitTid = null;
			if (tid != null && tid.hasMoreElements()) { 
				intuitTid = tid.nextElement();
			} else {				
				intuitTid = "Internal-" + UUID.randomUUID().toString();
			}
			MDC.put("intuitTid", intuitTid);
			filterChain.doFilter(req, res);	
			for(HystrixCommand<?> hystrixCommand : HystrixRequestLog.getCurrentRequest().getExecutedCommands()) {
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append("COMPID=").append(HYSTRIX_CMP_ID).append(" ");
				strBuilder.append(",intuitTid=").append(intuitTid).append(" ");
				strBuilder.append(",CompanyId=").append(companyId);
				strBuilder.append(",CommandGroup=").append(hystrixCommand.getCommandGroup().name());
				strBuilder.append(",Command=").append(hystrixCommand.getCommandKey().name());
				strBuilder.append(",ExecTime=").append(hystrixCommand.getExecutionTimeInMilliseconds());
				strBuilder.append(",CircuitOpen=").append(hystrixCommand.isCircuitBreakerOpen()?1:0);
				strBuilder.append(",Failed=").append(hystrixCommand.isFailedExecution()?1:0);
				strBuilder.append(",TimedOut=").append(hystrixCommand.isResponseTimedOut()?1:0);
				strBuilder.append(",ShortCircuited=").append(hystrixCommand.isResponseShortCircuited()?1:0);
				strBuilder.append(",Successful=").append(hystrixCommand.isSuccessfulExecution()?1:0);
				strBuilder.append(",Rejected=").append(hystrixCommand.isResponseRejected()?1:0);
				//Extra Log for debugging
				if (DynamicPropertyFactory.getInstance().getBooleanProperty(HYSTRIX_EXCEPTION_STACK_LOG_ENABLED, false).get()) {
					strBuilder.append(hystrixCommand.getFailedExecutionException()==null?"":(",Failed exec exception=\""))
					       .append(hystrixCommand.getFailedExecutionException().getMessage()).append("\"");
				} else {
					strBuilder.append(hystrixCommand.getFailedExecutionException()==null?"":(",Failed exec exception=\""))
				           .append(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(hystrixCommand.getFailedExecutionException())).append("\"");
				}
				Log.info(strBuilder.toString());
			}
	    }  finally {
	    	MDC.remove("intuitTid");
			context.shutdown();
		}
     }

	@Override
	public void destroy() {}

	@Override
	public void init(FilterConfig arg0) throws ServletException {}
}