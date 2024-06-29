package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;

@Data
public class EmailOrPasswordUpdate {
	private String appClientId;
	private String newEmail;
	
}
