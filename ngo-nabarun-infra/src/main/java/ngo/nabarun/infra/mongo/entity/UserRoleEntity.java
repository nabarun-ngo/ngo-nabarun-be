package ngo.nabarun.infra.mongo.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document("user_roles")
@Data
public class UserRoleEntity{
	
	@Id
	private String id;
	private String roleName;
	private String roleDisplayName;
	private String roleCode; //Technical role at auth0
	private String roleId; //Auth0 Id

	private Date createdOn;
	private String createdBy;
	
	private String userId;
	private Boolean active;
	private String profileId;
	
}