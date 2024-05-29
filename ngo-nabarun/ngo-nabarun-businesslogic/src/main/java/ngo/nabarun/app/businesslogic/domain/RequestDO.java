package ngo.nabarun.app.businesslogic.domain;

import java.util.ArrayList;
import java.util.List;
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
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AdditionalFieldSource;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.WorkFlowAction;
import ngo.nabarun.app.common.enums.WorkflowStatus;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkFlowDTO;
import ngo.nabarun.app.infra.dto.WorkListDTO;
import ngo.nabarun.app.infra.dto.WorkFlowDTO.WorkFlowDTOFilter;
import ngo.nabarun.app.infra.dto.WorkListDTO.WorkListDTOFilter;
import ngo.nabarun.app.infra.service.IUserInfraService;
import ngo.nabarun.app.infra.service.IWorkflowInfraService;

@Component
public class RequestDO {

	@Autowired
	private IWorkflowInfraService workflowInfraService;

	@Autowired
	private IUserInfraService userInfraService;

	@Autowired
	private BusinessHelper businessHelper;

	/**
	 * 
	 * @param index
	 * @param size
	 * @param filter
	 * @return
	 */
	public Paginate<RequestDetail> retrieveAllRequests(Integer index, Integer size, RequestDetailFilter filter) {
		WorkFlowDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new WorkFlowDTOFilter();
		}
		Page<RequestDetail> page = workflowInfraService.getWorkflows(index, size, filterDTO)
				.map(BusinessObjectConverter::toRequestDetail);
		return new Paginate<RequestDetail>(page);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public RequestDetail retrieveRequestDetails(String id) {
		WorkFlowDTO workflow = workflowInfraService.getWorkflow(id);
		return BusinessObjectConverter.toRequestDetail(workflow);
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
	public Paginate<RequestDetail> retrieveUserRequests(Integer index, Integer size, String userId, boolean isDelegated)
			throws Exception {
		WorkFlowDTOFilter filterDTO = new WorkFlowDTOFilter();
		UserDTO user = userInfraService.getUser(userId, IdType.AUTH_USER_ID, false);
		if (isDelegated) {
			filterDTO.setDelegatedRequesterId(user.getProfileId());
		} else {
			filterDTO.setRequesterId(user.getProfileId());
		}
		Page<RequestDetail> page = workflowInfraService.getWorkflows(index, size, filterDTO)
				.map(BusinessObjectConverter::toRequestDetail);
		return new Paginate<RequestDetail>(page);
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
	public Paginate<WorkDetail> retrieveUserWorkList(Integer index, Integer size, String userId, boolean isCompleted)
			throws Exception {
		WorkListDTOFilter filter = new WorkListDTOFilter();
		filter.setStepCompleted(isCompleted);
		if (isCompleted) {
			UserDTO tokenUser = userInfraService.getUser(userId, IdType.AUTH_USER_ID, false);
			filter.setDecisionMakerProfileId(tokenUser.getProfileId());
		} else {
			filter.setPendingWithUserId(userId);
		}
		Page<WorkDetail> page = workflowInfraService.getWorkList(index, size, filter)
				.map(m -> BusinessObjectConverter.toWorkItem(m));
		return new Paginate<WorkDetail>(page);

	}

	/**
	 * 
	 * @param workflowId
	 * @return
	 * @throws Exception
	 */
	public List<WorkDetail> retrieveWorkflowWorkList(String workflowId) throws Exception {
		WorkListDTOFilter filter = new WorkListDTOFilter();
		filter.setWorkflowId(workflowId);
		List<WorkListDTO> worklist = workflowInfraService.getWorkList(null, null, filter).getContent();
		return worklist.stream().map(m -> BusinessObjectConverter.toWorkItem(m)).toList();
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
	public RequestDetail createRequest(RequestDetail createRequest, boolean isPublicRequest,String creatorUserId,ActionFunction<WorkFlowAction, WorkFlowDTO,WorkFlowDTO> task)
			throws Exception {
		WorkFlowDTO workflow = businessHelper.convertToWorkflowDTO(createRequest.getType(),
				createRequest.getAdditionalFields());
		/*
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

		workflow.setWorkflowDescription(createRequest.getDescription());
		workflow.setId(businessHelper.generateWorkflowId());
		workflow = workflowInfraService.createWorkflow(workflow);
		WorkListDTO worklist = businessHelper.prepareWorkList(workflow.getWorkflowType(), workflow.getWorkflowStatus(),
				null);
		worklist.setPendingWithUsers(userInfraService.getUsersByRole(worklist.getPendingWithRoles()));
		workflow=task.exec(worklist.getCurrentAction(), workflow);
		worklist.setActionPerformed(workflow.isLastActionCompleted());
		worklist.setWorkflowId(workflow.getId());
		workflowInfraService.createWorkList(worklist);
		return BusinessObjectConverter.toRequestDetail(workflow);
	}

	/**
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public RequestDetail updateRequest(String id, RequestDetail request) throws Exception {
		WorkFlowDTO workflow = new WorkFlowDTO();
		if (request.getAdditionalFields() != null) {
			List<FieldDTO> addF = new ArrayList<>();
			for (AdditionalField attr : request.getAdditionalFields()) {
				if (attr.isUpdateField()) {
					addF.add(businessHelper.findAddtlFieldAndConvertToFieldDTO(AdditionalFieldSource.WORKFLOW, attr));
				}
			}
			workflow.setAdditionalFields(addF);
		}
		workflow.setWorkflowDescription(request.getDescription());
		workflow.setWorkflowName(request.getName());
		if (request.getStatus() != null && request.getStatus() == WorkflowStatus.CANCELLED) {
			workflow.setWorkflowStatus(request.getStatus());
			workflow.setRemarks(request.getRemarks());
			WorkListDTOFilter filter = new WorkListDTOFilter();
			filter.setWorkflowId(id);
			List<WorkListDTO> worklist = workflowInfraService.getWorkList(null, null, filter).getContent();
			for (WorkListDTO work : worklist) {
				work.setStepCompleted(true);
				workflowInfraService.updateWorkList(work.getId(), work);
			}
		}
		workflow = workflowInfraService.updateWorkflow(request.getId(), workflow);
		return BusinessObjectConverter.toRequestDetail(workflow);
	}

	/**
	 * 
	 * @param id
	 * @param request
	 * @param loggedInUserId
	 * @return
	 * @throws Exception
	 */

	public WorkDetail updateWorkItem(String id, WorkDetail request, String loggedInUserId,ActionFunction<WorkFlowAction, WorkFlowDTO,WorkFlowDTO> task) throws Exception {
		WorkListDTO workList = workflowInfraService.getWorkList(id);

		businessHelper.throwBusinessExceptionIf(() -> workList.getStepCompleted() == true,
				ExceptionEvent.GENERIC_ERROR);

		List<RoleDTO> tokenUserRoles = userInfraService.getUserRoles(loggedInUserId, IdType.AUTH_USER_ID, true);
		/*
		 * check if valid access
		 */
		String decisionGroup = null;
		List<String> rolegroups = businessHelper
				.getGroupsFromRole(tokenUserRoles.stream().map(m -> m.getCode()).collect(Collectors.toList()));
		for (String roleGroup : rolegroups) {
			if (workList.getPendingWithRoleGroups().contains(roleGroup)) {
				decisionGroup = roleGroup;
				break;
			}
		}
		boolean insufficientScope = decisionGroup == null;
		businessHelper.throwBusinessExceptionIf(() -> insufficientScope, ExceptionEvent.INSUFFICIENT_ACCESS);

		WorkFlowDTO workflow = workflowInfraService.getWorkflow(workList.getWorkflowId());

		/*
		 * Now update old worklist
		 */
		WorkListDTO wlDTO = new WorkListDTO();
		wlDTO.setDecision(request.getDecision());
		wlDTO.setDecisionDate(CommonUtils.getSystemDate());
		wlDTO.setDecisionMakerRoleGroup(decisionGroup);
		UserDTO tokenUser = userInfraService.getUser(loggedInUserId, IdType.AUTH_USER_ID, false);
		wlDTO.setDecisionMaker(tokenUser);
		wlDTO.setRemarks(request.getRemarks());
		wlDTO.setStepCompleted(true);

		/*
		 * next step based on decision and prepare next worklist
		 */

		WorkflowStatus nextStatus = businessHelper.getWorkflowNextStatus(workList.getWorkflowStatus(),
				workList.getWorkflowType(), request.getDecision());
		WorkListDTO nextWorkList = businessHelper.prepareWorkList(workList.getWorkflowType(), nextStatus,
				decisionGroup);

		nextWorkList.setPendingWithUsers(userInfraService.getUsersByRole(nextWorkList.getPendingWithRoles()));
		workflow=task.exec(nextWorkList.getCurrentAction(), workflow);
		//boolean isActionPerformed = performWorkflowAction(nextWorkList.getCurrentAction(), workflow);
		nextWorkList.setActionPerformed(workflow.isLastActionCompleted());
		nextWorkList.setWorkflowId(workflow.getId());

		nextWorkList = workflowInfraService.createWorkList(nextWorkList);
		workflowInfraService.updateWorkList(id, wlDTO);
		workflow.setWorkflowStatus(nextStatus);
		workflow = workflowInfraService.updateWorkflow(workList.getWorkflowId(), workflow);
		return BusinessObjectConverter.toWorkItem(nextWorkList);
	}
	
	
}
