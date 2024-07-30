package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;

@Data
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
	
	@JsonProperty("publicFlag")
	private Boolean publicFlag;
	
	@JsonProperty("status")
	private List<ProfileStatus> status;
	
	@JsonProperty("roles")
	private List<RoleCode> roles;
}
