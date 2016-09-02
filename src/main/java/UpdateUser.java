import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;


//Compensating logic is NOT a good use of fallback
public class UpdateUser extends HystrixCommand<User> {

    private final String id;
    public UpdateUser(String id) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestGroup")));
        this.id = id;
    }

    @Override 
    protected User run() {
    	return UserResource.update(id);
    }
    @Override
    protected User getFallback() {
    	return new RevertUserUpdate(id).execute();
    }
}

//@Override
//protected User run() {
//  User user = null;
//  try {
//       user = UserResource.update(id);
//  } catch(Exception ex) {
//       //1.  UpdateUser metric will be skewed due to the RevertUserUpdate.
//       //2.  Two threads will be consumed if both commands share the same thread pool.
//       user = new RevertUserUpdate(id).execute();
//  }
//  return user;
//}
