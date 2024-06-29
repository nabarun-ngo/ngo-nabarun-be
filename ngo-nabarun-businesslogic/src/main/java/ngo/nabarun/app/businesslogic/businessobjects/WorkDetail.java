package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.WorkflowDecision;
import ngo.nabarun.app.common.enums.WorkflowStatus;

@Data
public class WorkDetail {
	private String id;
	private String workflowId;
	private String description;
	private WorkType workType;
	private WorkflowStatus workflowStatus;
	private List<RoleCode> pendingWithRoles;
	private Date createdOn;
	private WorkflowDecision decision;
	private String remarks;
	private Boolean stepCompleted;
	private Date decisionDate;
	private UserDetail decisionOwner;

}
