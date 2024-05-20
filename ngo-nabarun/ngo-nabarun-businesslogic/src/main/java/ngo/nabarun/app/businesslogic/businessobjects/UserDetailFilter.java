package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailFilter {
	
	@JsonProperty("firstName")
	private String firstName;
	
	@JsonProperty("lastName")
	private String lastName;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("phoneNumber")
	private String phoneNumber;
	
	@JsonProperty("userId")
	private String userId;
	
}
