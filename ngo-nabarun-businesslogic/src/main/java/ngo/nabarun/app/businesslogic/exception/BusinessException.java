package ngo.nabarun.app.businesslogic.exception;

public class BusinessException extends Exception {

	private static final long serialVersionUID = 1L;

	public BusinessException(String message) {
		super(message);

	}

	public enum ExceptionEvent {
		GENERIC_ERROR,
		EMAIL_ALREADY_IN_USE, 
		PASSWORD_NOT_COMPLIANT, 
		INACTIVE_USER_DONATION,
		REGULAR_DONATION_EXISTS,
		//DONATION_ALREADY_RAISED,
		NO_AMOUNT_CHANGE_PAID,
		INACTIVE_USER_ACCOUNT, 
		OTP_EXPIRED, INVALID_OTP, INSUFFICIENT_ACCESS,
	}
}
