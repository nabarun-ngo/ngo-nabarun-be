package ngo.nabarun.app.businesslogic.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.IRequestBL;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.AdditionalConfigKey;
import ngo.nabarun.app.common.enums.AdditionalFieldSource;
import ngo.nabarun.app.common.enums.AddressType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkFlowAction;
import ngo.nabarun.app.common.enums.WorkflowStatus;
import ngo.nabarun.app.common.enums.WorkflowType;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkFlowDTO;
import ngo.nabarun.app.infra.dto.WorkFlowDTO.WorkFlowDTOFilter;
import ngo.nabarun.app.infra.dto.WorkListDTO;
import ngo.nabarun.app.infra.dto.WorkListDTO.WorkListDTOFilter;
import ngo.nabarun.app.infra.service.IUserInfraService;
import ngo.nabarun.app.infra.service.IWorkflowInfraService;

@Service
public class RequestBLImpl implements IRequestBL {

	@Autowired
	private IWorkflowInfraService workflowInfraService;

	@Autowired
	private IUserInfraService userInfraService;

	@Autowired
	private BusinessHelper businessHelper;

	@Autowired
	private GenericPropertyHelper propertyHelper;

	@Override
	public Paginate<RequestDetail> getRequests(Integer index, Integer size, RequestDetailFilter filter) {
		WorkFlowDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new WorkFlowDTOFilter();
		}
		Page<RequestDetail> page = workflowInfraService.getWorkflows(index, size, filterDTO)
				.map(DTOToBusinessObjectConverter::toRequestDetail);
		return new Paginate<RequestDetail>(page);
	}
	
	@Override
	public Paginate<RequestDetail> getMyRequests(Integer index, Integer size,boolean isDelegated) throws Exception {
		UserDTO tokenUser = userInfraService
				.getUser(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
						: SecurityUtils.getAuthUserId(), IdType.AUTH_USER_ID, false);
		WorkFlowDTOFilter filterDTO = new WorkFlowDTOFilter();
		if(isDelegated) {
			filterDTO.setDelegatedRequesterId(tokenUser.getProfileId());
		}else {
			filterDTO.setRequesterId(tokenUser.getProfileId());
		}
		Page<RequestDetail> page = workflowInfraService.getWorkflows(index, size, filterDTO)
				.map(DTOToBusinessObjectConverter::toRequestDetail);
		return new Paginate<RequestDetail>(page);
	}


	@Override
	public RequestDetail createRequest(RequestDetail createRequest) throws Exception {
		WorkFlowDTO workflow = businessHelper.convertToWorkflowDTO(createRequest.getType(),
				createRequest.getAdditionalFields());
		/*
		 * If it is a delegated request then token user will be delegated requester and
		 * requester details will be coming from ui json else set requester as token
		 * user
		 */
		workflow.setDelegated(createRequest.isDelegated());
		UserDTO tokenUser = userInfraService
				.getUser(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
						: SecurityUtils.getAuthUserId(), IdType.AUTH_USER_ID, false);
		if (createRequest.isDelegated()) {
			UserDTO requester = new UserDTO();
			requester.setProfileId(createRequest.getRequester().getId());
			requester.setFirstName(createRequest.getRequester().getFirstName());
			requester.setLastName(createRequest.getRequester().getLastName());
			requester.setName(createRequest.getRequester().getFullName());
			requester.setEmail(createRequest.getRequester().getEmail());
			workflow.setRequester(tokenUser);
			workflow.setDelegatedRequester(tokenUser);
		} else {
			workflow.setRequester(tokenUser);
		}
		
		workflow.setWorkflowDescription(createRequest.getDescription());
		workflow.setId(businessHelper.generateWorkflowId());
		workflow = workflowInfraService.createWorkflow(workflow);
		WorkListDTO worklist=businessHelper.prepareWorkList(workflow.getWorkflowType(),workflow.getWorkflowStatus(),null);
		worklist.setPendingWithUsers(userInfraService.getUsersByRole(worklist.getPendingWithRoles()));
		boolean isActionPerformed=performWorkflowAction(worklist.getCurrentAction(),workflow);
		worklist.setActionPerformed(isActionPerformed);
		worklist.setWorkflowId(workflow.getId());
		workflowInfraService.createWorkList(worklist);
		return DTOToBusinessObjectConverter.toRequestDetail(workflow);
	}
	
	@Override
	public RequestDetail updateRequest(String id, RequestDetail request) throws Exception {
		WorkFlowDTO workflow = new WorkFlowDTO();
		if(request.getAdditionalFields() != null) {
			List<FieldDTO> addF = new ArrayList<>();
			for(AdditionalField attr:request.getAdditionalFields()) {
				if(attr.isUpdateField()) {
					addF.add(businessHelper.findAddtlFieldAndConvertToFieldDTO(AdditionalFieldSource.WORKFLOW, attr));
				}
			}
			workflow.setAdditionalFields(addF);
		}
		workflow.setWorkflowDescription(request.getDescription());
		workflow.setWorkflowName(request.getName());
		if(request.getStatus() != null && request.getStatus() == WorkflowStatus.CANCELLED) {
			workflow.setWorkflowStatus(request.getStatus());
			workflow.setRemarks(request.getRemarks());
			WorkListDTOFilter filter= new WorkListDTOFilter();
			filter.setWorkflowId(id);
			List<WorkListDTO> worklist=workflowInfraService.getWorkList(null, null, filter).getContent();
			for(WorkListDTO work:worklist) {
				work.setStepCompleted(true);				
				workflowInfraService.updateWorkList(work.getId(), work);
			}
		}
		workflow=workflowInfraService.updateWorkflow(request.getId(), workflow);
		return DTOToBusinessObjectConverter.toRequestDetail(workflow);
	}
	

	
	@Override
	public Paginate<WorkDetail> getMyWorkList(Integer index, Integer size,boolean isCompleted) throws Exception {
		String userId=propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		WorkListDTOFilter filter = new WorkListDTOFilter();
		filter.setStepCompleted(isCompleted);
		if(isCompleted) {
			UserDTO tokenUser = userInfraService
					.getUser(userId,IdType.AUTH_USER_ID,false);
			filter.setDecisionMakerProfileId(tokenUser.getProfileId());	
		}else {
			filter.setPendingWithUserId(userId);
		}
		Page<WorkDetail> page=workflowInfraService.getWorkList(index, size, filter).map(m->DTOToBusinessObjectConverter.toWorkItem(m));
		return new Paginate<WorkDetail>(page);

	}

	@Override
	public List<WorkDetail> getWorkLists(String workflowId) throws Exception {
		WorkListDTOFilter filter = new WorkListDTOFilter();
		filter.setWorkflowId(workflowId);
		List<WorkListDTO> worklist=workflowInfraService.getWorkList(null, null, filter).getContent();
		return worklist.stream().map(m->DTOToBusinessObjectConverter.toWorkItem(m)).toList();
	}

	@Override
	public WorkDetail updateWorkList(String id, WorkDetail request) throws Exception {
		String userId=propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		WorkListDTO workList=workflowInfraService.getWorkList(id);
	
		businessHelper.throwBusinessExceptionIf(()->workList.getStepCompleted() == true, ExceptionEvent.GENERIC_ERROR);

		List<RoleDTO> tokenUserRoles =userInfraService.getUserRoles(userId, IdType.AUTH_USER_ID, true);
		/*
		 * check if valid access 
		 */
		String decisionGroup=null;
		List<String> rolegroups=businessHelper.getGroupsFromRole(tokenUserRoles.stream().map(m->m.getCode()).collect(Collectors.toList()));
		for(String roleGroup:rolegroups) {
			if(workList.getPendingWithRoleGroups().contains(roleGroup)) {
				decisionGroup=roleGroup;
				break;
			}
		}
		boolean insufficientScope=decisionGroup == null;
		businessHelper.throwBusinessExceptionIf(()->insufficientScope, ExceptionEvent.INSUFFICIENT_ACCESS);

		
		WorkFlowDTO workflow=workflowInfraService.getWorkflow(workList.getWorkflowId());
		
		/*
		 * Now update old worklist
		 */
		WorkListDTO wlDTO= new WorkListDTO();
		wlDTO.setDecision(request.getDecision());
		wlDTO.setDecisionDate(CommonUtils.getSystemDate());
		wlDTO.setDecisionMakerRoleGroup(decisionGroup);
		UserDTO tokenUser = userInfraService
				.getUser(userId,IdType.AUTH_USER_ID,false);
		wlDTO.setDecisionMaker(tokenUser);
		wlDTO.setRemarks(request.getRemarks());
		wlDTO.setStepCompleted(true);
		
		/*
		 * next step based on decision and prepare next worklist
		 */
		
		WorkflowStatus nextStatus=businessHelper.getWorkflowNextStatus( workList.getWorkflowStatus(), workList.getWorkflowType(), request.getDecision());
		WorkListDTO nextWorkList=businessHelper.prepareWorkList(workList.getWorkflowType(), nextStatus,decisionGroup);
		
		nextWorkList.setPendingWithUsers(userInfraService.getUsersByRole(nextWorkList.getPendingWithRoles()));
		boolean isActionPerformed=performWorkflowAction(nextWorkList.getCurrentAction(),workflow);
		nextWorkList.setActionPerformed(isActionPerformed);
		nextWorkList.setWorkflowId(workflow.getId());
			
		nextWorkList=workflowInfraService.createWorkList(nextWorkList);
		workflowInfraService.updateWorkList(id, wlDTO);
		workflow.setWorkflowStatus(nextStatus);
		workflow=workflowInfraService.updateWorkflow(workList.getWorkflowId(), workflow);
		return DTOToBusinessObjectConverter.toWorkItem(nextWorkList);
	}

	private boolean performWorkflowAction(WorkFlowAction action,WorkFlowDTO workflow) throws Exception {
//		if(!workflow.isLastActionCompleted() && workflow.getLastStatus() != null) {
//			WorkFlowAction lastAction = businessHelper.getWorkflowAction(workflow.getLastStatus(),
//					workflow.getWorkflowType());
//			performWorkflowAction(lastAction,workflow);
//		}
		switch (action) {
		case ONBOARD_USER:
			boolean emailVerified=workflow.getWorkflowType() == WorkflowType.JOIN_REQUEST;
			UserDTO memeber =onboardMember(workflow.getAdditionalFields(),emailVerified);
			workflow.setRequester(memeber);
			return true;
		case NO_ACTION:
			return false;
		}
		//workflow.setLastActionCompleted(true);	
		//workflowInfraService.updateWorkflow(workflow.getId(), workflow);
		return false;
	}

	

	private UserDTO onboardMember(List<FieldDTO> fields,boolean emailVerified) throws Exception {
		UserDTO userDTO = new UserDTO();
		PhoneDTO phoneDto = new PhoneDTO();
		AddressDTO addressDto = new AddressDTO();
		UserAdditionalDetailsDTO additionalDetailDto = new UserAdditionalDetailsDTO();
		for (FieldDTO field : fields) {
			switch (field.getFieldKey()) {
			case firstName:
				userDTO.setFirstName(field.getFieldValue());
				break;
			case lastName:
				userDTO.setLastName(field.getFieldValue());
				break;
			case email:
				userDTO.setEmail(field.getFieldValue());
				break;
			case dialCode:
				phoneDto.setPhoneCode(field.getFieldValue());
				break;
			case mobileNumber:
				phoneDto.setPhoneNumber(field.getFieldValue());
				phoneDto.setPhoneType(PhoneType.PRIMARY);
				break;
			case hometown:
				addressDto.setHometown(field.getFieldValue());
				addressDto.setAddressType(AddressType.PRESENT);
				break;
			case password:
				userDTO.setPassword(field.getFieldValue());
				break;
			default:
				break;
			}

		}
		additionalDetailDto.setActiveContributor(true);
		additionalDetailDto.setBlocked(false);
		additionalDetailDto.setDisplayPublic(false);
		additionalDetailDto.setEmailVerified(emailVerified);
		userDTO.setStatus(ProfileStatus.ACTIVE);
		userDTO.setPhoneNumber(phoneDto.getPhoneCode() + phoneDto.getPhoneNumber());
		userDTO.setPhones(List.of(phoneDto));
		userDTO.setAddresses(List.of(addressDto));
		userDTO.setAdditionalDetails(additionalDetailDto);
		String[] loginMethods = businessHelper.getAdditionalConfig(AdditionalConfigKey.LOGIN_METHODS).split(",");
		String defaultRoleCode = businessHelper.getAdditionalConfig(AdditionalConfigKey.DEFAULT_ROLE_CODE);
		userDTO.setLoginProviders(List.of(loginMethods));
		userDTO = userInfraService.createUser(userDTO);
		List<RoleDTO> roles = businessHelper.convertToRoleDTO(List.of(RoleCode.valueOf(defaultRoleCode)));
		userInfraService.updateUserRoles(userDTO.getProfileId(), roles);
		return userDTO;
	}

	@Override
	public RequestDetail getRequest(String id) {
		WorkFlowDTO workflow=workflowInfraService.getWorkflow(id);
		return DTOToBusinessObjectConverter.toRequestDetail(workflow);
	}

	

	
}
