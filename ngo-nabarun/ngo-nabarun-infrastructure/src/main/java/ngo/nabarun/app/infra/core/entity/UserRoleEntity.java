package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * MongoDB
 * DAO for storing user contact in DB
 */

@Document("user_roles")
@Data
public class UserRoleEntity{
	
	@Id
	private String id;
	private String roleName;
	private String roleDescription;
	private String extRoleId;
	private String roleCode;
	private String roleGroup;
	private Date startDate;
	private Date endDate;
	private String profileId;
	private String userId;
	private boolean active;

}
