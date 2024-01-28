package ngo.nabarun.app.infra.misc;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Deprecated
public class UserConfigTemplate {
	
	@JsonProperty("USER_TITLES")
	private List<KeyValuePair> userTitles;
	
	@JsonProperty("USER_GENDERS")
	private List<KeyValuePair> userGenders;
	
	@JsonProperty("DEFAULT_ROLE_CODE")
	private String defaultRoleCode;
	
	@JsonProperty("PASSWORD_VALIDITY")
	private String passwordValidity;
	
	@JsonProperty("PASSWORD_RESET_TICKET_VALIDITY")
	private String passwordResetTicketValidity;
	
	@JsonProperty("ROLE_GROUPS")
	private List<KeyValuePair> availableRoleGroups;
	
	@JsonProperty("USER_ROLES")
	private List<KeyValuePair> availableUserRoles;
}
