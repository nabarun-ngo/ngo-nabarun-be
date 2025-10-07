package ngo.nabarun.application.dto.result;

import lombok.Data;

@Data
public class RequestResult {
	private String id;
	private String name;
//	private RequestType type;
//	private RequestStatus status;
//	private String description;
//	private Date createdOn;
//	private Date resolvedOn;
//	private UserDetail requester;
//	private boolean delegated;
//	private UserDetail delegatedRequester;
//	private List<AdditionalField> additionalFields;
	//private List<WorkflowStepDetail> workflowSteps;
	private String remarks;


	@Data
	public static class RequestDetailFilter {
		private Boolean isDelegated;
	}

}