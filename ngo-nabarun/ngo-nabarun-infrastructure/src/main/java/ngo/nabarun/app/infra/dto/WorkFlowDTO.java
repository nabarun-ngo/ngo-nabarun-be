package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkflowStatus;
import ngo.nabarun.app.common.enums.WorkflowType;

@Data
public class WorkFlowDTO {
	private String id;
	private String workflowName;
	private WorkflowType workflowType;
	private WorkflowStatus workflowStatus;
	private WorkflowStatus lastStatus;
	private boolean lastActionCompleted;
	private String workflowDescription;
	private String remarks;
	private Date createdOn;
	private String createdBy;
	private Date resolvedOn;
	private UserDTO requester;
	private boolean delegated;
	private UserDTO delegatedRequester;
	private List<FieldDTO> additionalFields;

	
	@Data
	public static class WorkFlowDTOFilter{
		private String workflowId;
		private List<WorkflowType> workflowType;
		private List<WorkflowStatus> workflowStatus;
		private Date fromDate;
		private Date toDate;
		private String requesterId;
		private String delegatedRequesterId;
		private List<RoleCode> pendingWithRoles;

	}
	
	

}
