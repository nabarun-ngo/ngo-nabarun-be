package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.RequestStatus;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.util.CommonUtils;

@Data
public class RequestDTO {
	private String id;
	private String workflowName;
	private RequestType type;
	//private String typeValue;

	private RequestStatus status;
	//private String statusValue;

	private RequestStatus lastStatus;
	private boolean lastActionCompleted;
	private String description;
	private String remarks;
	private Date createdOn;
	private String createdBy;
	private Date resolvedOn;
	private UserDTO requester;
	private boolean delegated;
	private UserDTO delegatedRequester;
	private List<FieldDTO> additionalFields;
	private boolean systemGenerated;
	private UserDTO systemRequestOwner;

	public Map<String,Object> toMap(Map<String,String> domainMap){
		 Map<String,Object> map= new HashMap<>();
		 map.put("id", id);
		 map.put("workflowName", workflowName);
		 map.put("type", type == null ? null : domainMap.get(type.name()));
		 map.put("description", workflowName);
		 map.put("status", status == null ? null : domainMap.get(status.name()));
		 map.put("createdOn", CommonUtils.formatDateToString(createdOn, "dd MMM yyyy hh:mm:ss a", "IST"));
		 map.put("resolvedOn", CommonUtils.formatDateToString(createdOn, "dd MMM yyyy hh:mm:ss a", "IST"));
		 map.put("delegatedRequester", delegated ? delegatedRequester.toMap(domainMap) :null);
		 map.put("requester", requester != null ? requester.toMap(domainMap):null);
		 map.put("remarks", remarks);

		 return map;
	}

	
	@Data
	public static class RequestDTOFilter{
		private String id;
		private List<RequestType> type;
		private List<RequestStatus> workflowStatus;
		private Date fromDate;
		private Date toDate;
		private String requesterId;
		private String delegatedRequesterId;
		private List<RoleCode> pendingWithRoles;

	}
	
	

}
