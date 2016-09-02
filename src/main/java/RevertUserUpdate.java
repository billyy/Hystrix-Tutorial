import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class RevertUserUpdate extends HystrixCommand<User> {

    private final String id;
    public RevertUserUpdate(String id) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestGroup")));
        this.id = id;
    }
	@Override
	protected User run() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
