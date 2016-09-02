import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

public class UpdateUserFacade extends HystrixCommand<User> {

    private final String id;
    public UpdateUserFacade(String id) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestGroup"))
        		.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
        	    .withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)));
        
        
        this.id = id;
    }
    
	@Override
	protected User run() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
