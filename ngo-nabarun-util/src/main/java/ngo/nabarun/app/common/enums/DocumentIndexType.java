package ngo.nabarun.app.common.enums;

import lombok.Getter;

public enum DocumentIndexType {
	DONATION("DONATION_DOCUMENTS"),
	EVENT("EVENT_DOCUMENTS"),
	NOTICE("NOTICE_DOCUMENTS"),
	USER("USER_DOCUMENTS"), 
	PROFILE_PHOTO("PROFILE_PHOTOS"), 
	EVENT_COVER("EVENT_COVER_PICTURES"), 
	REQUEST("REQUEST"),
	EXPENSE("EXPENSE"),
	TRANSACTION("TRANSACTIONS");
	
	@Getter
	private String docFolderName;

	DocumentIndexType(String docFolderName) {
		this.docFolderName=docFolderName;
	}
	
	
}
