package ngo.nabarun.app.infra.dto;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.RoleGroup;

@Data
public class RoleDTO {
	private String id;
	private String name;
	private String displayName;
	private RoleCode code;
	private RoleGroup group;
	private String description;
}
