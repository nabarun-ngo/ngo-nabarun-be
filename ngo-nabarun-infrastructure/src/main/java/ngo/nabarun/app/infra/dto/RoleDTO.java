package ngo.nabarun.app.infra.dto;

import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;

@Data
public class RoleDTO {
	private String id;
	private String auth0Id;
	private String name;
	private RoleCode code;
	//private List<RoleGroup> groups;
	private List<String> groups;

	private String description;
}
