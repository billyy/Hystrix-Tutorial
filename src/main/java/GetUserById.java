import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class GetUserById extends HystrixCommand<User> {

    private final String id;
    public GetUserById(String id) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestGroup")));
        this.id = id;
    }
     
    @Override
    protected User run() {
        return UserResource.getUserById(id);
    }

   //Fetch from a separate cache (fast and reliable)
    @Override
    protected User getFallback() {        
        return (User)Cache.get(id);
    }
}

