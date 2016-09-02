import com.netflix.hystrix.exception.HystrixRuntimeException;

public class SampleCall {
	public void updated()  {
		//Updated with Hystrix
		try {
			//Better approach is to wrap this class and return the original exception.
			User user = new GetUserById("111").execute();
		} catch(HystrixRuntimeException ex) {
			if (ex.getCause() instanceof NullPointerException) {
				//Do Something
			}
		}		
	}
	
	public void original() {
		
		//Original
		try {
			User user = UserResource.getUserById("111");
		} catch(NullPointerException ex) {
			//Do Something
		}
	}
	
	public void good() {
		//The same can be written as follow.
		User user = null;
		
		try {
			user = new UpdateUser("111").execute();
		} catch(Exception ex) {
			user = new RevertUserUpdate("111").execute();
		}
	}
}
