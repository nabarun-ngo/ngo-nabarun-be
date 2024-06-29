package ngo.nabarun.app.common.exception;

public class NotFoundException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NotFoundException(String subject,String id) {
		super("No "+subject+" found with "+id+" .");
	}

}
