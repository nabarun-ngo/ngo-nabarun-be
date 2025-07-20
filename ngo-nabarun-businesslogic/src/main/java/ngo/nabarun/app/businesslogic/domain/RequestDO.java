package ngo.nabarun.app.businesslogic.domain;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail.RequestDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail.WorkDetailFilter;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.ActionFunction;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.WorkAction;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.RequestStatus;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;
import ngo.nabarun.app.infra.dto.RequestDTO.RequestDTOFilter;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.WorkDTO.WorkDTOFilter;
import ngo.nabarun.app.infra.service.IUserInfraService;
import ngo.nabarun.app.infra.service.IWorkflowInfraService;

@Component
public class RequestDO extends CommonDO {

	@Autowired
	private IUserInfraService userInfraService;

	@Autowired
	private IWorkflowInfraService workflowInfraService;

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
		Page<RequestDTO> page = workflowInfraService.getRequests(index, size, filterDTO);
		return new Paginate<RequestDTO>(page);
	}

	public Paginate<WorkDTO> retrieveAllWorkItems(Integer index, Integer size, WorkDetailFilter filter) {
		WorkDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new WorkDTOFilter();
			filterDTO.setStepCompleted(filter.getCompleted() == Boolean.TRUE);
			filter.setRequestId(filter.getRequestId());
			filter.setWorkId(filter.getWorkId());
			filter.setFromDate(filter.getFromDate());
			filter.setToDate(filter.getToDate());
			filter.setSourceType(filter.getSourceType());
		}
		return retrieveAllWorkItems(index, size, filterDTO);
	}

	public Paginate<WorkDTO> retrieveAllWorkItems(Integer index, Integer size, WorkDTOFilter filterDTO) {
		Page<WorkDTO> page = workflowInfraService.getWorkItems(index, size, filterDTO);
		return new Paginate<WorkDTO>(page);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public RequestDTO retrieveRequestDetails(String id) {
		return workflowInfraService.getRequest(id);// BusinessObjectConverter.toRequestDetail(workflow);
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
	public Paginate<RequestDTO> retrieveUserRequests(Integer index, Integer size, String userId,
			RequestDetailFilter filter) throws Exception {
		RequestDTOFilter filterDTO = new RequestDTOFilter();
		UserDTO user = userInfraService.getUser(userId, IdType.AUTH_USER_ID, false);
		if (filter.getIsDelegated() == Boolean.TRUE) {
			filterDTO.setDelegatedRequesterId(user.getProfileId());
		} else {
			filterDTO.setRequesterId(user.getProfileId());
		}
		Page<RequestDTO> page = workflowInfraService.getRequests(index, size, filterDTO);
		/* .map(BusinessObjectConverter::toRequestDetail); */
		return new Paginate<RequestDTO>(page);
	}

	public RequestDTO createRequest(RequestDetail createRequest, boolean isPublicRequest, String creatorUserId,
			ActionFunction<WorkAction, RequestDTO, RequestDTO> task) throws Exception {
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setType(createRequest.getType());
		requestDTO.setDelegated(createRequest.isDelegated());
		requestDTO.setRequester(BusinessObjectConverter.toUserDTO(createRequest.getRequester()));
		requestDTO.setDescription(createRequest.getDescription());
		requestDTO.setAdditionalFields(BusinessObjectConverter.toFieldDTO(createRequest.getAdditionalFields()));
		return createRequest(requestDTO, isPublicRequest, creatorUserId, task);
	}

	/**
	 * 
	 * @param createRequest
	 * @param isPublicRequest
	 * @param creatorUserId   This will be null for a public request
	 * @param task
	 * @return
	 * @throws Exception
	 */
	public RequestDTO createRequest(RequestDTO createRequest, boolean isPublicRequest, String creatorUserId,
			ActionFunction<WorkAction, RequestDTO, RequestDTO> task) throws Exception {

		RequestDTO workflow = businessDomainHelper.convertToRequestDTO(createRequest.getType(),
				createRequest.getAdditionalFields());
		workflow.setRefId(createRequest.getRefId());
		if (createRequest.isSystemGenerated()) {
			workflow.setSystemRequestOwner(createRequest.getSystemRequestOwner());
			UserDTO requester = new UserDTO();
			requester.setName("System");
			workflow.setRequester(requester);
		}

		/**
		 * If it is a public request then no delegation is possible If it is a delegated
		 * request then delegated_requester = token_user and requester =
		 * UI_provided_requester else requester = token_user
		 */

		if (isPublicRequest) {
			workflow.setDelegated(false);
			workflow.setRequester(createRequest.getRequester());
		} else if (createRequest.isDelegated()) {
			workflow.setDelegated(true);
			workflow.setRequester(createRequest.getRequester());
			// TODO Check if this can be reduced
			UserDTO tokenUser = userInfraService.getUser(creatorUserId, IdType.AUTH_USER_ID, false);
			workflow.setDelegatedRequester(tokenUser);
		} else if (!createRequest.isSystemGenerated()) {
			// TODO Check if this can be reduced
			UserDTO tokenUser = userInfraService.getUser(creatorUserId, IdType.AUTH_USER_ID, false);
			workflow.setRequester(tokenUser);
		}

		if (createRequest.getDescription() != null) {
			workflow.setDescription(createRequest.getDescription());
		}
		workflow.setId(generateWorkflowId());
		workflow = workflowInfraService.createRequest(workflow);

		createTasksForRequest(createRequest, workflow, task);
		return workflow;
	}

	
	private void createTasksForRequest(RequestDTO createRequest, RequestDTO workflow,
			ActionFunction<WorkAction, RequestDTO, RequestDTO> task) throws Exception {
		
		submitJob(workflow.getId(), "Create Tasks for Request", (job)->{
			job.setInput(Map.of("createRequest", createRequest, "workflow", workflow, "task", task));
			try {
				job.log("Preparing draft WorkList from business helper");
				WorkDTO workItem = businessDomainHelper.prepareWorkList(workflow.getType(), workflow.getStatus(), null);
				job.log("Draft workItem = " + CommonUtils.toJSONString(workItem, false));
				/*
				 * Update dash board count
				 */
				if (workflow.getRequester() != null && workflow.getRequester().getUserId() != null) {
					String requesterUserId = workflow.getRequester().getUserId();
					job.log("Sending Updates to " + requesterUserId);
					updateAndSendDashboardCounts(requesterUserId, data -> {
						Map<String, String> map = new HashMap<>();
						RequestDTOFilter filterDTO = new RequestDTOFilter();
						filterDTO.setRequesterUserId(requesterUserId);
						filterDTO.setResolved(false);
						Page<RequestDTO> page = workflowInfraService.getRequests(null, null, filterDTO);
						map.put(BusinessConstants.attr_DB_requestCount, String.valueOf(page.getNumberOfElements()));
						return map;
					});
				}
				job.log("Creating workitem ...");
				workItem = createWorkItem(job, workflow, workItem, task);
				job.log("Workitem created...");
				if (!createRequest.isSystemGenerated()) {
					job.log("Sending Email....");

					/**
					 * Sending email to requester and delegated requester
					 */
					List<CorrespondentDTO> corrDTO = new ArrayList<>();
					corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO)
							.email(workflow.getRequester().getEmail()).name(workflow.getRequester().getName()).build());
					Map<String, Object> workflow_vars = workflow.toMap(businessDomainHelper.getDomainKeyValues());
					if (workflow.isDelegated()) {
						corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.CC)
								.email(workflow.getDelegatedRequester().getEmail())
								.name(workflow.getDelegatedRequester().getName()).build());
						sendEmail(BusinessConstants.EMAILTEMPLATE__ON_REQUEST_CREATION_DELEGATED, corrDTO,
								Map.of("request", workflow_vars));
					} else {
						sendEmail(BusinessConstants.EMAILTEMPLATE__ON_REQUEST_CREATION, corrDTO,
								Map.of("request", workflow_vars));
					}
				}
				return true;
			} catch (Exception e) {
				job.setError(e);
				return false;
			} 
		});
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
					FieldDTO field = new FieldDTO();
					field.setFieldValue(attr.getValue());
					field.setFieldKey(attr.getKey());
					field.setFieldSource(id);
				}
			}
			workflow.setAdditionalFields(addF);
		}
		workflow.setDescription(request.getDescription());
		// workflow.setWorkflowName(request.getName());
		if (request.getStatus() != null && request.getStatus() == RequestStatus.CANCELLED) {
			workflow.setStatus(request.getStatus());
			workflow.setRemarks(request.getRemarks());
			workflow.setResolvedOn(CommonUtils.getSystemDate());
			WorkDTOFilter filter = new WorkDTOFilter();
			filter.setWorkflowId(id);
			List<WorkDTO> worklist = workflowInfraService.getWorkItems(null, null, filter).getContent();
			for (WorkDTO work : worklist) {
				work.setStepCompleted(true);
				workflowInfraService.updateWorkItem(work.getId(), work);
			}
		}
		workflow = workflowInfraService.updateRequest(id, workflow);
		if (workflow.getResolvedOn() != null && workflow.getRequester() != null
				&& workflow.getRequester().getUserId() != null) {
			String requesterUserId = workflow.getRequester().getUserId();
			updateAndSendDashboardCounts(requesterUserId, data -> {
				Map<String, String> map = new HashMap<>();
				RequestDTOFilter filterDTO = new RequestDTOFilter();
				filterDTO.setRequesterUserId(requesterUserId);
				filterDTO.setResolved(false);
				Page<RequestDTO> page = workflowInfraService.getRequests(null, null, filterDTO);
				map.put(BusinessConstants.attr_DB_requestCount, String.valueOf(page.getNumberOfElements()));
				return map;
			});
		}
		return workflow;
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
	public Paginate<WorkDTO> retrieveUserWorkList(Integer index, Integer size, String userId,
			WorkDetailFilter workFilter) throws Exception {
		WorkDTOFilter filter = new WorkDTOFilter();
		filter.setStepCompleted(workFilter.getCompleted() == Boolean.TRUE);
		if (workFilter.getCompleted() == Boolean.TRUE) {
			UserDTO tokenUser = userInfraService.getUser(userId, IdType.AUTH_USER_ID, false);
			filter.setDecisionMakerProfileId(tokenUser.getProfileId());
		} else {
			filter.setPendingWithUserId(userId);
		}
		filter.setWorkflowId(workFilter.getRequestId());
		filter.setId(workFilter.getWorkId());
		filter.setFromDate(workFilter.getFromDate());
		filter.setToDate(workFilter.getToDate());
		filter.setSourceType(workFilter.getSourceType());
		Page<WorkDTO> page = workflowInfraService.getWorkItems(index, size, filter);
		return new Paginate<WorkDTO>(page);

	}

	/**
	 * 
	 * @param workflowId
	 * @return
	 * @throws Exception
	 */
	public List<WorkDTO> retrieveWorkflowWorkList(String workflowId) throws Exception {
		WorkDTOFilter filter = new WorkDTOFilter();
		filter.setWorkflowId(workflowId);
		List<WorkDTO> worklist = workflowInfraService.getWorkItems(null, null, filter).getContent();
		return worklist;
	}

	public WorkDTO createWorkItem(JobDTO job, RequestDTO workflow, WorkDTO workItem,
			ActionFunction<WorkAction, RequestDTO, RequestDTO> task) throws Exception {
		// System.err.println("Final Step1 = "+workItem.isFinalStep());
		job.log("Retrieveing Additional field DTO");
		List<FieldDTO> fieldList = businessDomainHelper
				.findAddtlFieldDTOList("WORKITEM-" + workItem.getWorkSourceStatus());
		workItem.setAdditionalFields(fieldList);
		workItem.setPendingWithUsers(new ArrayList<>());

		if (workItem.getPendingWithRoles() != null) {
			workItem.getPendingWithUsers().addAll(userInfraService.getUsersByRole(workItem.getPendingWithRoles()));
		}

		if (workflow.getSystemRequestOwner() != null) {
			workItem.getPendingWithUsers().add(workflow.getSystemRequestOwner());
		}

		workflow = task.exec(workItem.getCurrentAction(), workflow);
		workItem.setActionPerformed(workflow.isLastActionCompleted());
		workItem.setWorkSourceId(workflow.getId());
		workItem.setWorkSourceRefId(workflow.getRefId());
		/***
		 * We will not create any work item if Work type is NA Also not sent and email
		 * to any user with whom the work is pending as the WORKTYPE is NA and no work
		 * need to be performed All other actions will be performed
		 */
		if (workItem.getWorkType() != WorkType.NA) {
			workItem.setId(generateWorkId());
			workItem = workflowInfraService.createWorkItem(workItem);
			job.log("Created workitem " + CommonUtils.toJSONString(workItem, false));
			/**
			 * Update Dashboard count
			 */
			if (workItem.getPendingWithUsers() != null) {
				for (UserDTO pendingUser : workItem.getPendingWithUsers()) {
					updateAndSendDashboardCounts(pendingUser.getUserId(), data -> {
						Map<String, String> map = new HashMap<>();
						WorkDTOFilter filterDTO = new WorkDTOFilter();
						filterDTO.setPendingWithUserId(pendingUser.getUserId());
						filterDTO.setStepCompleted(false);
						Page<WorkDTO> page = workflowInfraService.getWorkItems(null, null, filterDTO);
						map.put(BusinessConstants.attr_DB_workCount, String.valueOf(page.getNumberOfElements()));
						return map;
					});
				}
			}
			/**
			 * Sending email and notification to concerned persons
			 */
			if (workItem.getPendingWithUsers() != null && !workItem.getPendingWithUsers().isEmpty()) {
				List<CorrespondentDTO> corrDTO = new ArrayList<>();
				for (UserDTO user : workItem.getPendingWithUsers()) {
					corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.BCC)
							.email(user.getEmail()).name(user.getName()).build());
				}
				Map<String, Object> work_item_vars = workItem.toMap(businessDomainHelper.getDomainKeyValues());
				sendEmail(BusinessConstants.EMAILTEMPLATE__ON_WORK_CREATION, corrDTO,
						Map.of("workItem", work_item_vars));
				sendNotification(BusinessConstants.NOTIFICATION__ON_WORK_CREATION, Map.of("workItem", work_item_vars),
						workItem.getPendingWithUsers(),workItem.getId());
			}
		}
		return workItem;
	}

	/**
	 * 
	 * @param id
	 * @param request
	 * @param loggedInUserId
	 * @return 
	 * @return
	 * @throws Exception
	 * @Note Check Vaid Access >>
	 */
	public WorkDTO updateWorkItem(String id, WorkDetail request, String loggedInUserId,
			ActionFunction<WorkAction, RequestDTO, RequestDTO> task) throws Exception {
		WorkDTO currentWorkDTO = workflowInfraService.getWorkItem(id);
		return updateWorkItem(currentWorkDTO, request, loggedInUserId, task);
	}

	public WorkDTO updateWorkItem(WorkDTO currentWorkDTO, WorkDetail request, String loggedInUserId,
			ActionFunction<WorkAction, RequestDTO, RequestDTO> task) throws Exception {

		businessDomainHelper.throwBusinessExceptionIf(() -> currentWorkDTO.getStepCompleted() == true,
				ExceptionEvent.GENERIC_ERROR);

		String decisionGroup = null;
		if (currentWorkDTO.getWorkType() == WorkType.USER_INPUT) {
			boolean hasScope = currentWorkDTO.getPendingWithUsers().stream()
					.anyMatch(m -> m.getUserId().equalsIgnoreCase(loggedInUserId));
			businessDomainHelper.throwBusinessExceptionIf(() -> !hasScope, ExceptionEvent.INSUFFICIENT_ACCESS);
		} else {
			List<RoleDTO> tokenUserRoles = userInfraService.getUserRoles(loggedInUserId, IdType.AUTH_USER_ID, true);
			/**
			 * check if valid access
			 */
			List<String> rolegroups = businessDomainHelper
					.getGroupsFromRole(tokenUserRoles.stream().map(m -> m.getCode()).collect(Collectors.toList()));
			for (String roleGroup : rolegroups) {
				if (currentWorkDTO.getPendingWithRoleGroups().contains(roleGroup)) {
					decisionGroup = roleGroup;
					break;
				}
			}
			boolean insufficientScope = decisionGroup == null;
			businessDomainHelper.throwBusinessExceptionIf(() -> insufficientScope, ExceptionEvent.INSUFFICIENT_ACCESS);
		}

		/**
		 * We are setting additional fields and preparing based on which next step will
		 * be derived but we will not mark this step as completed We will mark this step
		 * as completed only after Next step is successfully created
		 */
		WorkDTO currWorkDTOUpdate = new WorkDTO();
		if (request.getAdditionalFields() != null) {
			List<FieldDTO> addF = new ArrayList<>();
			for (AdditionalField attr : request.getAdditionalFields()) {
				if (attr.isUpdateField()) {
					FieldDTO field = new FieldDTO();
					field.setFieldValue(attr.getValue());
					field.setFieldKey(attr.getKey());
					field.setFieldSource(currentWorkDTO.getId());
					addF.add(field);
				}
			}
			currWorkDTOUpdate.setAdditionalFields(addF);
		}
		currWorkDTOUpdate.setDecisionDate(CommonUtils.getSystemDate());
		currWorkDTOUpdate.setDecisionMakerRoleGroup(decisionGroup);
		// TODO check if this can be replaced
		UserDTO tokenUser = userInfraService.getUser(loggedInUserId, IdType.AUTH_USER_ID, false);
		currWorkDTOUpdate.setDecisionMaker(tokenUser);

		/**
		 * fetching decision and remarks from the current work based on which next step
		 * will be derived
		 */
		String decisionOrConfirmation = null;
		String remarks = null;
		if (currentWorkDTO.getWorkType() == WorkType.DECISION) {
			FieldDTO decisionField = currWorkDTOUpdate.getAdditionalFields().stream()
					.filter(f -> f.getFieldKey() == AdditionalFieldKey.decision).findFirst().orElseThrow();
			decisionOrConfirmation = decisionField.getFieldValue();
		} else if (currentWorkDTO.getWorkType() == WorkType.CONFIRMATION) {
			FieldDTO decisionField = currWorkDTOUpdate.getAdditionalFields().stream()
					.filter(f -> f.getFieldKey() == AdditionalFieldKey.confirmation).findFirst().orElseThrow();
			decisionOrConfirmation = decisionField.getFieldValue();
		}
		Optional<FieldDTO> remarksField = currWorkDTOUpdate.getAdditionalFields() == null ? Optional.empty()
				: currWorkDTOUpdate.getAdditionalFields().stream()
						.filter(f -> f.getFieldKey() == AdditionalFieldKey.remarks).findFirst();
		remarks = remarksField.isEmpty() ? null : remarksField.get().getFieldValue();

		createTasksAndUpdateCurrTask(currentWorkDTO, task, currWorkDTOUpdate, decisionOrConfirmation, decisionGroup,
				remarks);
		return currentWorkDTO;
	}

	
	private void createTasksAndUpdateCurrTask(WorkDTO currentWorkDTO,
			ActionFunction<WorkAction, RequestDTO, RequestDTO> task, WorkDTO currWorkDTOUpdate,
			String decisionOrConfirmation, String decisionGroup, String remarks) throws Exception {
		submitJob(currentWorkDTO.getWorkSourceRefId(), "createTasksAndUpdateCurrTask", (job)->{
			try {
				RequestDTO workflowDTO = workflowInfraService.getRequest(currentWorkDTO.getWorkSourceId());

				/**
				 * Deriving next step based on current work decision and prepare and create next
				 * work after completing current actions once the next work is created we will
				 * update the current work as complete
				 */
				RequestStatus nextStatus = businessDomainHelper.getWorkflowNextStatus(currentWorkDTO.getWorkSourceStatus(),
						currentWorkDTO.getWorkSourceType(), decisionOrConfirmation);

				WorkDTO nextWorkDTO = businessDomainHelper.prepareWorkList(currentWorkDTO.getWorkSourceType(), nextStatus,
						decisionGroup);
				nextWorkDTO = createWorkItem(new JobDTO(), workflowDTO, nextWorkDTO, task);

				/**
				 * Now Next step/task has been successfully created We will mark current work as
				 * completed we will send notification to requester and delegated requester
				 * Regarding work update
				 */
				currWorkDTOUpdate.setStepCompleted(true);
				WorkDTO currWorkDTOUpdate_1 = workflowInfraService.updateWorkItem(currentWorkDTO.getId(), currWorkDTOUpdate);
				/**
				 * Update Dashboard count
				 */
				if (currWorkDTOUpdate_1.getPendingWithUsers() != null) {
					for (UserDTO pendingUser : currWorkDTOUpdate_1.getPendingWithUsers()) {
						updateAndSendDashboardCounts(pendingUser.getUserId(), data -> {
							Map<String, String> map = new HashMap<>();
							WorkDTOFilter filterDTO = new WorkDTOFilter();
							filterDTO.setPendingWithUserId(pendingUser.getUserId());
							filterDTO.setStepCompleted(false);
							Page<WorkDTO> page = workflowInfraService.getWorkItems(null, null, filterDTO);
							map.put(BusinessConstants.attr_DB_workCount, String.valueOf(page.getNumberOfElements()));
							return map;
						});
					}
				}

				List<UserDTO> notifyUser = new ArrayList<>();
				notifyUser.add(workflowDTO.getRequester());
				if (workflowDTO.isDelegated()) {
					notifyUser.add(workflowDTO.getDelegatedRequester());
				}
				sendNotification(BusinessConstants.NOTIFICATION__ON_WORK_CLOSURE, Map.of("work", currWorkDTOUpdate_1),
						notifyUser,workflowDTO.getId());

				/**
				 * Next task is created based on current task and current task is marked
				 * completed Now Updating the workflow with next work details
				 */
				workflowDTO.setStatus(nextStatus);
				workflowDTO.setRemarks(remarks);
				if (nextWorkDTO.isFinalStep()) {
					workflowDTO.setResolvedOn(CommonUtils.getSystemDate());
				}
				workflowDTO = workflowInfraService.updateRequest(currentWorkDTO.getWorkSourceId(), workflowDTO);

				/**
				 * After updating the workflow If next work step is final step and if current
				 * action is performed Then send 'REQUEST_CLOSURE' email to REQUESTER as no
				 * further action to be done on this workflow re
				 */
				if (!workflowDTO.isSystemGenerated() && nextWorkDTO.getActionPerformed() && nextWorkDTO.isFinalStep()) {
					List<CorrespondentDTO> corrDTO = new ArrayList<>();
					corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO)
							.email(workflowDTO.getRequester().getEmail()).name(workflowDTO.getRequester().getName())
							.build());
					if (workflowDTO.isDelegated()) {
						corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.CC)
								.email(workflowDTO.getDelegatedRequester().getEmail())
								.name(workflowDTO.getDelegatedRequester().getName()).build());
					}
					Map<String, Object> work_flow_vars = workflowDTO.toMap(businessDomainHelper.getDomainKeyValues());
					sendEmail(BusinessConstants.EMAILTEMPLATE__ON_REQUEST_CLOSURE, corrDTO,
							Map.of("request", work_flow_vars));
				}
				return true;
			} catch (Exception e) {
				job.setError(e);
				return false;
			}
		});
	}

	public void sendTaskReminderEmail(JobDTO job) throws Exception {
		job.log("[INFO] Starting TASK REMINDER Job");

		job.log("[INFO] Retrieveing and grouping all open tasks");
		WorkDetailFilter workFilter = new WorkDetailFilter();
		workFilter.setCompleted(false);
		Map<UserDTO, List<WorkDTO>> groupedByPendingWithUsers = retrieveAllWorkItems(null, null, workFilter)
				.getContent().stream().filter(work -> work.getPendingWithUsers() != null) // Handle null
																							// pendingWithUsers lists
				.flatMap(work -> work.getPendingWithUsers().stream()
						.map(user -> new AbstractMap.SimpleEntry<>(user, work))) // Create pairs of UserDTO and WorkDTO
				.collect(Collectors.groupingBy(Map.Entry::getKey,
						Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
		job.log("[INFO] Retrieved " + groupedByPendingWithUsers.entrySet().size() + "  groupd open tasks by assignee");

		for (Entry<UserDTO, List<WorkDTO>> workitem : groupedByPendingWithUsers.entrySet()) {
			UserDTO assignee = workitem.getKey();
			List<WorkDTO> workList = workitem.getValue();
			job.log("[INFO] ------ Found " + workList.size() + " open tasks for user " + assignee.getName() + "("
					+ assignee.getProfileId() + ") ----");

			List<Map<String, Object>> task_vars = workList.stream().map(m -> {
				try {
					return m.toMap(businessDomainHelper.getDomainKeyValues());
				} catch (Exception e) {
					job.log("[ERROR] Unable to retrieve getDomainKeyValues().");
					return null;
				}
			}).collect(Collectors.toList());
			job.log("[INFO] Sending email to " + assignee.getName() + "(" + assignee.getProfileId() + ").");
			CorrespondentDTO recipient = CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO)
					.email(assignee.getEmail()).name(assignee.getName()).build();
			Map<String, Object> user_vars = workitem.getKey().toMap(businessDomainHelper.getDomainKeyValues());
			sendEmail(BusinessConstants.EMAILTEMPLATE__WORKITEM_REMINDER, List.of(recipient),
					Map.of("workItems", task_vars, "user", user_vars, "currentDate",
							CommonUtils.formatDateToString(CommonUtils.getSystemDate(), "dd MMM yyyy", "IST")));
			job.log("[INFO] Email sent!");
			job.log("[INFO] ------------------");
		}
		job.log("[INFO] Ending TASK REMINDER Job");

	}

}
