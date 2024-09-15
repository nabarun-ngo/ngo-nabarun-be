package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ngo.nabarun.app.common.enums.RoleCode;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

	@JsonProperty("roleId")
	private String roleId;

	@JsonProperty("roleName")
	private String roleName;

	@JsonProperty("roleCode")
	private RoleCode roleCode;

//	@JsonProperty("roleGroup")
//	private RoleGroup roleGroup;

	@JsonProperty("description")
	private String description;
}
