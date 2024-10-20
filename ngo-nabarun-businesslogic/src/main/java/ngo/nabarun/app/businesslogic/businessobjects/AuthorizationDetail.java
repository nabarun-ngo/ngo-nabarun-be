package ngo.nabarun.app.businesslogic.businessobjects;


import lombok.Data;
import ngo.nabarun.app.common.enums.AuthRefType;

@Deprecated
@Data
public class AuthorizationDetail {

	private String authorizationCode;
	private String authorizationState;
	
	private String callbackUrl;
	private AuthRefType authRefType;
	private String authRefId;

}
