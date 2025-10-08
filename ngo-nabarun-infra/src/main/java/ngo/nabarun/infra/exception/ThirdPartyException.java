package ngo.nabarun.infra.exception;

public class ThirdPartyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ThirdPartySystem system;

	public ThirdPartyException(Throwable cause, ThirdPartySystem system) {
		super(cause);
		this.system = system;
	}

	public ThirdPartyException(String message, ThirdPartySystem system) {
		super(message);
		this.system = system;
	}

	@Override
	public String getMessage() {
		return system == null ? super.getMessage() : system.name() + " : " + super.getMessage();
	}

	public enum ThirdPartySystem {
		FIREBASE, SENDGRID, AUTH0
	}

}
