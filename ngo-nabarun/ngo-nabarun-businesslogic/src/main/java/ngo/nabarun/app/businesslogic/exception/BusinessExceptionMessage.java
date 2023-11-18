package ngo.nabarun.app.businesslogic.exception;

import java.util.Map;

public enum BusinessExceptionMessage{
	DONATION_ALREADY_RAISED("Donation is already raised between selected Start Date and End Date. Please select different date range."),
	INACTIVE_DONOR("User is currently not active payer."), 
	USER_AUTH_NEEDED("User must be authenticated to perform this."),
	INVALID_DATA("Invalid data provided at ${FIELD_NAME} field(s)."),
	NULL_OR_EMPTY_DATA("Null or empty data provided at ${FIELD_NAME} field(s)."),
	INVALID_STATE("INVALID OBJECT STATE")

	;
	private String message;

	private BusinessExceptionMessage(String message) {
		this.message=message;
	}

	public String getMessage(Map<String,String> replace) {
		for(String key:replace.keySet()) {
			message=message.replace("${"+key+"}", replace.get(key));
		}
		return message;
	}
	
	public String getMessage() {
		return message;
	}
}
