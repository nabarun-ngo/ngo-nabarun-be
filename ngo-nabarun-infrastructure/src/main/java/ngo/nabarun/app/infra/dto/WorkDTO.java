package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkAction;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.WorkDecision;
import ngo.nabarun.app.common.enums.RequestStatus;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.util.CommonUtils;

@Data
public class WorkDTO {
	private String id;
	private String workSourceId;
	private String workSourceRefId;
	private String description;
	private RequestStatus workSourceStatus;
	private RequestType workSourceType;
	private WorkType workType;

	private List<UserDTO> pendingWithUsers;
	private boolean groupWork;
	private List<RoleCode> pendingWithRoles;
	private List<String> pendingWithRoleGroups;
	private Date createdOn;
	private WorkDecision decision;
	private UserDTO decisionMaker;
	private String decisionMakerRoleGroup;
	private String remarks;
	private WorkAction currentAction;
	private Boolean actionPerformed;
	private Boolean stepCompleted;
	private Date decisionDate;
	private boolean finalStep;
	private List<FieldDTO> additionalFields;


	@Data
	public static class WorkDTOFilter{
		private Boolean stepCompleted;
		private String pendingWithUserId;
		private List<RoleCode> pendingWithRoles;
		private String decisionMakerProfileId;
		private String workflowId;
		private String id;
		private Date fromDate;
		private Date toDate;
		private RequestType sourceType;
		private String sourceRefId;

	}


	public Map<String, Object> toMap(Map<String, String> domainKeyValues) {
		Map<String, Object> workItem = new HashMap<>();
		workItem.put("id", id);
		workItem.put("workSourceId", workSourceId);
		workItem.put("workItemName", workSourceStatus == null ? null : domainKeyValues.get(workSourceStatus.name()));
		workItem.put("workSourceType", workSourceType == null ? null : domainKeyValues.get(workSourceType.name()));
		workItem.put("description", description);
		workItem.put("type", workType == null ? null : (domainKeyValues.get(workType.name()) == null ? workType.name() :domainKeyValues.get(workType.name())));
		workItem.put("createdOn", CommonUtils.formatDateToString(createdOn, "dd MMM yyyy", "IST"));
		workItem.put("closedOn", CommonUtils.formatDateToString(decisionDate, "dd MMM yyyy", "IST") );
		workItem.put("remarks", remarks);
		workItem.put("decision", decision == null ? null : decision.getValue());
		workItem.put("decisionMaker", decisionMaker == null ? null : decisionMaker.toMap(domainKeyValues));
		workItem.put("workType", workType != null && workSourceStatus != null ? workItem.get("type")+" ("+workItem.get("workItemName")+")":"");
		workItem.put("assignedTo", pendingWithUsers != null ? String.join(", ",pendingWithUsers.stream().map(m->m.getName()).collect(Collectors.toList())) :"");

		return workItem;
	}
}