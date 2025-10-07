package ngo.nabarun.domain;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, Exception e) {
		super(message, e);
	}

	public BusinessException(ExceptionEvent event) {
		super(event.message);
	}

	public enum ExceptionEvent {
		GENERIC_ERROR("Something went wrong."),
		EMAIL_ALREADY_IN_USE("The provided email is already exists in our system."), PASSWORD_NOT_COMPLIANT,
		INACTIVE_USER_DONATION, REGULAR_DONATION_EXISTS,
		// DONATION_ALREADY_RAISED,
		NO_AMOUNT_CHANGE_PAID, INACTIVE_USER_ACCOUNT, OTP_EXPIRED, INVALID_OTP, INSUFFICIENT_ACCESS,
		TITLE_GENDER_MISALIGNED, UNRESOLVED_DONATION_EXISTS, ACCOUNT_WITH_BALANCE_EXISTS, INSUFFICIENT_ACCOUNT_BALANCE;

		private String message;

		ExceptionEvent(String msg) {
			this.message = msg;
		}

		ExceptionEvent() {
			this.message = this.name();
		}
	}
}