package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkFlowAction;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.WorkflowDecision;
import ngo.nabarun.app.common.enums.WorkflowStatus;
import ngo.nabarun.app.common.enums.WorkflowType;

@Data
public class WorkListDTO {
	private String id;
	private String workflowId;
	private String description;
	private WorkflowStatus workflowStatus;
	private WorkflowType workflowType;
	private WorkType workType;

	private List<UserDTO> pendingWithUsers;
	private boolean groupWork;
	private List<RoleCode> pendingWithRoles;
	private List<String> pendingWithRoleGroups;
	private Date createdOn;
	private WorkflowDecision decision;
	private UserDTO decisionMaker;
	private String decisionMakerRoleGroup;
	private String remarks;
	private WorkFlowAction currentAction;
	private Boolean actionPerformed;
	private Boolean stepCompleted;
	private Date decisionDate;

	@Data
	public static class WorkListDTOFilter{
		private boolean stepCompleted;
		private String pendingWithUserId;
		private List<RoleCode> pendingWithRoles;
		private String decisionMakerProfileId;
		private String workflowId;
	}
}