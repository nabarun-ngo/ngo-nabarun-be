package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class UserAdditionalDetailsDTO {
		private Date createdOn;
		private Date updatedOn;
		private String createdBy;
		private boolean blocked;
		private boolean activeContributor;
		private boolean displayPublic;
		private Boolean emailVerified;
		private Date lastLogin;
		private Date lastPasswordChange;
		private int loginsCount;
		private Map<String,Object> attributes= new HashMap<>();
		private Boolean passwordResetRequired;
		private Date donPauseStartDate;
		private Date donPauseEndDate;


}
