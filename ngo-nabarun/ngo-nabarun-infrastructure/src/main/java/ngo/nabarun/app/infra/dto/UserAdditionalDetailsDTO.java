package ngo.nabarun.app.infra.dto;

import java.util.Date;

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
}
