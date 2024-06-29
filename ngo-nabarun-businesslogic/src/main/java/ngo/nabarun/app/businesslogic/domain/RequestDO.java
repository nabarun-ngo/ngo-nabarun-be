package ngo.nabarun.app.businesslogic.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField; 
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.ActionFunction;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AdditionalFieldSource;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.WorkFlowAction;
import ngo.nabarun.app.common.enums.WorkflowStatus;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;
import ngo.nabarun.app.infra.dto.RequestDTO.RequestDTOFilter;
import ngo.nabarun.app.infra.dto.WorkDTO.WorkListDTOFilter;
import ngo.nabarun.app.infra.service.IUserInfraService;
import ngo.nabarun.app.infra.service.IWorkflowInfraService;

@Component
public class RequestDO extends CommonDO{

	@Autowired
	private IWorkflowInfraService workflowInfraService;

	@Autowired
	private IUserInfraService userInfraService;

	/**
	 * 
	 * @param index
	 * @param size
	 * @param filter
	 * @return
	 */
	public Paginate<RequestDTO> retrieveAllRequests(Integer index, Integer size, RequestDetailFilter filter) {
		RequestDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new RequestDTOFilter();
		}
		Page<RequestDTO> page = workflowInfraService.getWorkflows(index, size, filterDTO);
		return new Paginate<RequestDTO>(page);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public RequestDTO retrieveRequestDetails(String id) {
		return workflowInfraService.getWorkflow(id);//BusinessObjectConverter.toRequestDetail(workflow);
	}

	/**
	 * 
	 * @param index
	 * @param size
	 * @param userId
	 * @param isDelegated
	 * @return
	 * @throws Exception
	 */
	public Paginate<RequestDTO> retrieveUserRequests(Integer index, Integer size, String userId, boolean isDelegated)
			throws Exception {
		RequestDTOFilter filterDTO = new RequestDTOFilter();
		UserDTO user = userInfraService.getUser(userId, IdType.AUTH_USER_ID, false);
		if (isDelegated) {
			filterDTO.setDelegatedRequesterId(user.getProfileId());
		} else {
			filterDTO.setRequesterId(user.getProfileId());
		}
		Page<RequestDTO> page = workflowInfraService.getWorkflows(index, size, filterDTO);
				/*.map(BusinessObjectConverter::toRequestDetail);*/
		return new Paginate<RequestDTO>(page);
	}

	/**
	 * 
	 * @param index
	 * @param size
	 * @param userId
	 * @param isCompleted
	 * @return
	 * @throws Exception
	 */
	public Paginate<WorkDTO> retrieveUserWorkList(Integer index, Integer size, String userId, boolean isCompleted)
			throws Exception {
		WorkListDTOFilter filter = new WorkListDTOFilter();
		filter.setStepCompleted(isCompleted);
		if (isCompleted) {
			UserDTO tokenUser = userInfraService.getUser(userId, IdType.AUTH_USER_ID, false);
			filter.setDecisionMakerProfileId(tokenUser.getProfileId());
		} else {
			filter.setPendingWithUserId(userId);
		}
		Page<WorkDTO> page = workflowInfraService.getWorkList(index, size, filter);
				//;
		return new Paginate<WorkDTO>(page);

	}

	/**
	 * 
	 * @param workflowId
	 * @return
	 * @throws Exception
	 */
	public List<WorkDTO> retrieveWorkflowWorkList(String workflowId) throws Exception {
		WorkListDTOFilter filter = new WorkListDTOFilter();
		filter.setWorkflowId(workflowId);
		List<WorkDTO> worklist = workflowInfraService.getWorkList(null, null, filter).getContent();
		return worklist;
	}

	/**
	 * 
	 * @param createRequest
	 * @param isPublicRequest 
	 * @param creatorUserId This will be null for a public request
	 * @param task
	 * @return
	 * @throws Exception
	 */
	public RequestDTO createRequest(RequestDetail createRequest, boolean isPublicRequest,String creatorUserId,ActionFunction<WorkFlowAction, RequestDTO,RequestDTO> task)
			throws Exception {
		RequestDTO workflow = businessDomainHelper.convertToWorkflowDTO(createRequest.getType(),
				createRequest.getAdditionalFields());
		/**
		 * If it is a public request then no delegation is possible
		 * If it is a delegated request then delegated_requester = token_user and requester = UI_provided_requester
		 * else requester = token_user
		 */
		
		if(isPublicRequest) {
			workflow.setDelegated(false);
			workflow.setRequester(BusinessObjectConverter.toUserDTO(createRequest.getRequester()));
		}else if (createRequest.isDelegated()) {
			workflow.setDelegated(true);
			workflow.setRequester(BusinessObjectConverter.toUserDTO(createRequest.getRequester()));
			UserDTO tokenUser = userInfraService.getUser(creatorUserId, IdType.AUTH_USER_ID, false);
			workflow.setDelegatedRequester(tokenUser);
		} else {
			UserDTO tokenUser = userInfraService.getUser(creatorUserId, IdType.AUTH_USER_ID, false);
			workflow.setRequester(tokenUser);
		}

		workflow.setDescription(createRequest.getDescription());
		workflow.setId(generateWorkflowId());
		workflow = workflowInfraService.createWorkflow(workflow);
		WorkDTO workItem = businessDomainHelper.prepareWorkList(workflow.getType(), workflow.getStatus(),
				null);
		workItem=createWorkItem(workflow,workItem,task);
		/**
		 * Sending email to requester and delegated requester
		 */
		List<CorrespondentDTO> corrDTO=new ArrayList<>();
		corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO).email(workflow.getRequester().getEmail()).name(workflow.getRequester().getName()).build());
		if(workflow.isDelegated()) {
			corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.CC).email(workflow.getDelegatedRequester().getEmail()).name(workflow.getDelegatedRequester().getName()).build());
			sendEmail(BusinessConstants.EMAILTEMPLATE__ON_REQUEST_CREATION_DELEGATED, corrDTO, Map.of("request",workflow));
		}else {
			sendEmail(BusinessConstants.EMAILTEMPLATE__ON_REQUEST_CREATION, corrDTO, Map.of("request",workflow));
		}
		return workflow;
	}
	
	public WorkDTO createWorkItem(RequestDTO workflow, WorkDTO workItem,ActionFunction<WorkFlowAction, RequestDTO,RequestDTO> task) throws Exception {
		//System.err.println("Final Step1 = "+workItem.isFinalStep()); 
		workItem.setId(generateWorkId());
		workItem.setPendingWithUsers(userInfraService.getUsersByRole(workItem.getPendingWithRoles()));
		workflow=task.exec(workItem.getCurrentAction(), workflow);
		workItem.setActionPerformed(workflow.isLastActionCompleted());
		workItem.setWorkflowId(workflow.getId());
		System.err.println("cash"+workItem.getPendingWithUsers());

		workItem=workflowInfraService.createWorkList(workItem);
		//System.err.println("Final Step = "+workItem.isFinalStep());
		
		workItem.setWfTypeValue(businessDomainHelper.getDisplayValue(workItem.getWorkflowType().name()));
		workItem.setWfStatusValue(businessDomainHelper.getDisplayValue(workItem.getWorkflowStatus().name()));
		
		/**
		 * Sending email and notification to concerned persons
		 */
		System.err.println("cash2"+workItem.getPendingWithUsers());
		if(workItem.getPendingWithUsers() != null && !workItem.getPendingWithUsers().isEmpty()) {
			List<CorrespondentDTO> corrDTO=new ArrayList<>();
			for(UserDTO user:workItem.getPendingWithUsers()) {
				corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.BCC).email(user.getEmail()).name(user.getName()).build());
			}
			sendEmail(BusinessConstants.EMAILTEMPLATE__ON_WORK_CREATION, corrDTO, Map.of("work",workItem));
			sendNotification(BusinessConstants.NOTIFICATION__ON_WORK_CREATION , Map.of("work",workItem), workItem.getPendingWithUsers());
		}
		
		return workItem;
	}

	/**
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public RequestDTO updateRequest(String id, RequestDetail request) throws Exception {
		RequestDTO workflow = new RequestDTO();
		if (request.getAdditionalFields() != null) {
			List<FieldDTO> addF = new ArrayList<>();
			for (AdditionalField attr : request.getAdditionalFields()) {
				if (attr.isUpdateField()) {
					addF.add(businessDomainHelper.findAddtlFieldAndConvertToFieldDTO(AdditionalFieldSource.WORKFLOW, attr));
				}
			}
			workflow.setAdditionalFields(addF);
		}
		workflow.setDescription(request.getDescription());
		workflow.setWorkflowName(request.getName());
		if (request.getStatus() != null && request.getStatus() == WorkflowStatus.CANCELLED) {
			workflow.setStatus(request.getStatus());
			workflow.setRemarks(request.getRemarks());
			WorkListDTOFilter filter = new WorkListDTOFilter();
			filter.setWorkflowId(id);
			List<WorkDTO> worklist = workflowInfraService.getWorkList(null, null, filter).getContent();
			for (WorkDTO work : worklist) {
				work.setStepCompleted(true);
				workflowInfraService.updateWorkList(work.getId(), work);
			}
		}
		workflow = workflowInfraService.updateWorkflow(request.getId(), workflow);
		return workflow;
	}

	/**
	 * 
	 * @param id
	 * @param request
	 * @param loggedInUserId
	 * @return
	 * @throws Exception
	 */

	public WorkDTO updateWorkItem(String id, WorkDetail request, String loggedInUserId,ActionFunction<WorkFlowAction, RequestDTO,RequestDTO> task) throws Exception {
		WorkDTO workList = workflowInfraService.getWorkList(id);

		businessDomainHelper.throwBusinessExceptionIf(() -> workList.getStepCompleted() == true,
				ExceptionEvent.GENERIC_ERROR);

		List<RoleDTO> tokenUserRoles = userInfraService.getUserRoles(loggedInUserId, IdType.AUTH_USER_ID, true);
		/**
		 * check if valid access
		 */
		String decisionGroup = null;
		List<String> rolegroups = businessDomainHelper
				.getGroupsFromRole(tokenUserRoles.stream().map(m -> m.getCode()).collect(Collectors.toList()));
		for (String roleGroup : rolegroups) {
			if (workList.getPendingWithRoleGroups().contains(roleGroup)) {
				decisionGroup = roleGroup;
				break;
			}
		}
		boolean insufficientScope = decisionGroup == null;
		businessDomainHelper.throwBusinessExceptionIf(() -> insufficientScope, ExceptionEvent.INSUFFICIENT_ACCESS);

		RequestDTO workflow = workflowInfraService.getWorkflow(workList.getWorkflowId());

		/**
		 * Now update old work list
		 */
		WorkDTO wlDTO = new WorkDTO();
		wlDTO.setDecision(request.getDecision());
		wlDTO.setDecisionDate(CommonUtils.getSystemDate());
		wlDTO.setDecisionMakerRoleGroup(decisionGroup);
		UserDTO tokenUser = userInfraService.getUser(loggedInUserId, IdType.AUTH_USER_ID, false);
		wlDTO.setDecisionMaker(tokenUser);
		wlDTO.setRemarks(request.getRemarks());
		wlDTO.setStepCompleted(true);
		wlDTO=workflowInfraService.updateWorkList(id, wlDTO);

		/**
		 * Sending notification to requester and delegated requester
		 * Regarding work item update
		 */
		List<UserDTO> notifyUser= new ArrayList<>();
		notifyUser.add(workflow.getRequester());
		if(workflow.isDelegated()) {
			notifyUser.add(workflow.getDelegatedRequester());
		}
		sendNotification(BusinessConstants.NOTIFICATION__ON_WORK_CLOSURE , Map.of("work",wlDTO), notifyUser);


		/**
		 * next step based on decision and prepare next work list and create it
		 */

		WorkflowStatus nextStatus = businessDomainHelper.getWorkflowNextStatus(workList.getWorkflowStatus(),
				workList.getWorkflowType(), request.getDecision());
		
		
		WorkDTO nextWorkList = businessDomainHelper.prepareWorkList(workList.getWorkflowType(), nextStatus,
				decisionGroup);
		//System.err.println("i=> "+nextWorkList.isFinalStep());
		nextWorkList = createWorkItem(workflow, nextWorkList, task);
		
		/**
		 * Updating next workflow status to workflow
		 */
		workflow.setStatus(nextStatus);
		workflow = workflowInfraService.updateWorkflow(workList.getWorkflowId(), workflow);
		/**
		 * ON COMPLETION
		 * When step = final step and action performed
		 * Then send email to re
		 */
		//if final step then check if action performed so update step completed on action performed
		System.err.println("action performed = "+nextWorkList.getActionPerformed() +" final step ="+nextWorkList.isFinalStep());
		if(nextWorkList.getActionPerformed() && nextWorkList.isFinalStep()) {
			System.err.println("All step completed");
			List<CorrespondentDTO> corrDTO=new ArrayList<>();
			corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO).email(workflow.getRequester().getEmail()).name(workflow.getRequester().getName()).build());
			if(workflow.isDelegated()) {
				corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.CC).email(workflow.getDelegatedRequester().getEmail()).name(workflow.getDelegatedRequester().getName()).build());
			}
			sendEmail(BusinessConstants.EMAILTEMPLATE__ON_REQUEST_CLOSURE, corrDTO, Map.of("request",workflow,"work",nextWorkList));

		}
		return nextWorkList;
	}
	
	
}
