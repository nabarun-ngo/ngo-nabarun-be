package ngo.nabarun.app.ext.exception;

import ngo.nabarun.app.ext.helpers.ThirdPartySystem;

public class ThirdPartyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ThirdPartySystem system;

	
	public ThirdPartyException(Throwable cause,ThirdPartySystem system) {
		super(cause);
		this.system=system;
	}


	@Override
	public String getMessage() {
		return system == null ? super.getMessage() : system.name()+" : "+ super.getMessage();
	}

	
}
