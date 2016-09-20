package com.sample.logging;


import com.netflix.hystrix.HystrixInvokableInfo;
import com.netflix.hystrix.HystrixRequestLog;
import com.sample.utils.RequestScopeObject;

/**
 * Record metrics for all Hystrix Commands for a request.   This code can be used in a servlet filter.
 * Additional metrics (fallback status) can be found in the Hystrix wiki.
 * @author byuen
 *
 */
public class LoggingHelper {
    public static void log() {
        for (HystrixInvokableInfo<?> hystrixCommand : HystrixRequestLog.getCurrentRequest().getAllExecutedCommands()) {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("tid=").append(RequestScopeObject.get());
            strBuilder.append(",CommandGroup=").append(hystrixCommand.getCommandGroup().name());
            strBuilder.append(",Command=").append(hystrixCommand.getCommandKey().name());
            strBuilder.append(",ExecTime=").append(hystrixCommand.getExecutionTimeInMilliseconds());
            strBuilder.append(",CircuitOpen=").append(hystrixCommand.isCircuitBreakerOpen() ? 1 : 0);
            strBuilder.append(",Failed=").append(hystrixCommand.isFailedExecution() ? 1 : 0);
            strBuilder.append(",TimedOut=").append(hystrixCommand.isResponseTimedOut() ? 1 : 0);
            strBuilder.append(",ShortCircuited=").append(hystrixCommand.isResponseShortCircuited() ? 1 : 0);
            strBuilder.append(",Successful=").append(hystrixCommand.isSuccessfulExecution() ? 1 : 0);
            strBuilder.append(",Rejected=").append(hystrixCommand.isResponseRejected() ? 1 : 0);
            System.out.println(strBuilder.toString());
        }

    }

}