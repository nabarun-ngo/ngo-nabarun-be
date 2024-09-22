package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.WorkflowStatus;

@Data
public class WorkDetail {
	private String id;
	private String workflowId;
	private String description;
	private WorkType workType;
	private WorkflowStatus workflowStatus;
	private List<RoleCode> pendingWithRoles;
	private List<UserDetail> pendingWith;

	private Date createdOn;
	//private WorkDecision decision;
	//private String remarks;
	private Boolean stepCompleted;
	private Date decisionDate;
	private UserDetail decisionOwner;
	private List<AdditionalField> additionalFields;
	
	@Data
	public static class WorkDetailFilter {
		@JsonProperty("workId")
		private String workId;
		
		@JsonProperty("requestId")
		private String requestId;
		
		@JsonProperty("completed")
		private Boolean completed;
		
		@JsonProperty("fromDate")
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private Date fromDate;
		
		@JsonProperty("toDate")
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private Date toDate;
	}

}
