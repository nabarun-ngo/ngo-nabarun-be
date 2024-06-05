package ngo.nabarun.app.businesslogic.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.exception.BusinessCondition;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.common.enums.AdditionalConfigKey;
import ngo.nabarun.app.common.enums.AdditionalFieldSource;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.NotificationType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkFlowAction;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.WorkflowDecision;
import ngo.nabarun.app.common.enums.WorkflowStatus;
import ngo.nabarun.app.common.enums.WorkflowType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;
import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;
import ngo.nabarun.app.infra.service.IGlobalDataInfraService;

@Component
public class BusinessDomainHelper {

	@Autowired
	private IGlobalDataInfraService domainInfraService;
	private static Map<String, List<KeyValuePair>> domainConfig = null;
	private static Map<String, String> domainKeyValue = new HashMap<>();

	private static final String SPLITTER = ",";
	private static final String STAR = "*";

	private static final String ITEM_USER_TITLE = "USER_TITLES";
	private static final String ITEM_USER_GENDER = "USER_GENDERS";
	private static final String ITEM_AVAILABLE_ROLE = "USER_ROLES";
	private static final String ITEM_AVAILABLE_ROLE_GROUP = "USER_ROLE_GROUPS";
	private static final String ITEM_DONATION_STATUS = "DONATION_STATUSES";
	private static final String ITEM_DONATION_TYPE = "DONATION_TYPES";
	private static final String ITEM_PAYMENT_METHODS = "PAYMENT_METHODS";
	private static final String ITEM_UPI_OPTIONS = "UPI_OPTIONS";
	private static final String ITEM_ADDITIONAL_FIELDS = "ADDITIONAL_FIELDS";
	private static final String ITEM_PASSWORD_COMPLEXITY_OPTIONS = "PASSWORD_COMPLEXITY_OPTIONS";
	private static final String ITEM_NABARUN_ORG_INFO = "NABARUN_ORG_INFO";
	private static final String ITEM_NABARUN_RULES_REGULATIONS = "NABARUN_RULES_REGULATIONS";
	private static final String ITEM_BUSINESS_EXCEPTION_MESSAGES = "BUSINESS_EXCEPTION_MESSAGES";
	private static final String ITEM_EMAIL_TEMPLATE_CONFIG = "EMAIL_TEMPLATE_CONFIG";
	private static final String ITEM_NOTIFICATION_TEMPLATE_CONFIG = "NOTIFICATION_TEMPLATE_CONFIG";

	private static final String ITEM_DONATION_TYPE__ATTR_DEFAULT_STATUS = "DEFAULT_STATUS";
	private static final String ITEM_DONATION_TYPE__ATTR_DEFAULT_AMOUNT = "DEFAULT_AMOUNT";
	private static final String ITEM_DONATION_TYPE__ATTR_LAST_PAYMENT_DAY = "LAST_PAYMENT_DAY";

	private static final String ITEM_USER_TITLE__ATTR_GENDER = "GENDER";
	private static final String ITEM_DONATION_STATUS__ATTR_IS_FINAL_STATUS = "IS_FINAL_STATUS";
	private static final String ITEM_DONATION_STATUS__ATTR_NEXT_STATUS = "NEXT_STATUS";
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_TYPE = "TYPE";
	private static final String ITEM_AVAILABLE_ROLE__ATTR_GROUPS = "GROUP";

	private static final String ITEM_WORKFLOW_TYPE = "WORKFLOW_TYPES";
	private static final String ITEM_WORKFLOW_TYPE__ATTR_DEFAULT_STEP = "DEFAULT_STEP";

	private static final String ITEM_ADDITIONAL_CONFIG = "ADDITIONAL_CONFIG";
	private static final String ITEM_WORKFLOW_STEP = "WORKFLOW_STEPS";
	private static final String ITEM_WORKFLOW_STEP__ATTR_IS_FINAL_STEP = "IS_FINAL_STEP";
	private static final String ITEM_WORKFLOW_STEP__ATTR_CURRENT_ACTION = "CURRENT_ACTION";
	private static final String ITEM_WORKFLOW_STEP__ATTR_IS_DECISION_STEP = "IS_DECISION_STEP";
	private static final String ITEM_WORKFLOW_STEP__ATTR_NEXT_STEP = "NEXT_STEP";
	private static final String ITEM_WORKFLOW_STEP__ATTR_DECISION_MAKER_ROLE_GROUPS = "DECISION_MAKER_ROLE_GROUPS";

	protected Map<String, List<KeyValuePair>> getDomainConfigs() throws Exception {
		if (domainConfig == null) {
			domainConfig = domainInfraService.getDomainRefConfigs();
		}
		return domainConfig;
		// return domainInfraService.getDomainRefConfigs();
	}

	public String getDisplayValue(String key) throws Exception {
		if (domainKeyValue.isEmpty()) {
			Map<String, List<KeyValuePair>> configs = getDomainConfigs();
			for (Entry<String, List<KeyValuePair>> config : configs.entrySet()) {
				for (KeyValuePair item : config.getValue()) {
					domainKeyValue.put(item.getKey(), item.getValue());
				}
			}
		}
		return domainKeyValue.get(key);
	}

	protected List<KeyValuePair> getDomainConfig(String name) throws Exception {
		return getDomainConfigs().get(name);
	}

	/*
	 * *****************************************************************************
	 * ******************* CONFIG
	 * *****************************************************************************
	 * *******************
	 */

	public String getAdditionalConfig(AdditionalConfigKey key) throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		KeyValuePair config = domainRef.get(ITEM_ADDITIONAL_CONFIG).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(key.name())).findFirst()
				.orElseThrow(() -> new Exception("No such config found."));
		return config.getValue();
	}

	/*
	 * *****************************************************************************
	 * ******************* USER & Roles
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * Checks if title and gender is aligned
	 * 
	 * @param title
	 * @param gender
	 * @return returns true if titlre and gender is aligned
	 * @throws Exception
	 */
	public boolean isTitleGenderAligned(String title, String gender) throws Exception {
		List<KeyValuePair> kvTitle = getDomainConfig(ITEM_USER_TITLE);

		Optional<KeyValuePair> userTitle = kvTitle.stream().filter(f -> f.getKey().equalsIgnoreCase(title)).findFirst();
		if (userTitle.isEmpty()) {
			return false;
		}
		String allowedGender = String.valueOf(userTitle.get().getAttributes().get(ITEM_USER_TITLE__ATTR_GENDER));
		return allowedGender.contains(STAR) || allowedGender.toUpperCase().contains(gender.toUpperCase());
	}

	/**
	 * Get reference data for user
	 * 
	 * @return Map of list of KeyValue objects for user
	 * @throws Exception
	 */
	public Map<String, List<KeyValue>> getUserRefData() throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		obj.put("userTitles", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_USER_TITLE)));
		obj.put("userGenders", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_USER_GENDER)));
		obj.put("availableRoles", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_AVAILABLE_ROLE)));
		obj.put("availableRoleGroups",
				BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_AVAILABLE_ROLE_GROUP)));
		return obj;
	}

	/**
	 * Converts
	 * 
	 * @return Map of list of KeyValue objects for user
	 * @throws Exception
	 */
	public List<RoleDTO> convertToRoleDTO(List<RoleCode> roleCodes) throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		List<RoleDTO> obj = new ArrayList<>();
		for (KeyValuePair roleObj : domainRef.get(ITEM_AVAILABLE_ROLE)) {
			if (roleCodes.contains(RoleCode.valueOf(roleObj.getKey().trim()))) {
				RoleDTO role = new RoleDTO();
				role.setCode(RoleCode.valueOf(roleObj.getKey()));
				role.setDescription(roleObj.getDescription());
				String groups = roleObj.getAttributes().get(ITEM_AVAILABLE_ROLE__ATTR_GROUPS).toString();
				// role.setGroups(List.of(groups.split(",")).stream().map(m->RoleGroup.valueOf(m)).toList());
				role.setGroups(List.of(groups.split(SPLITTER)));
				role.setName(roleObj.getValue());
				obj.add(role);
			}
		}
		return obj;
	}

	public List<RoleCode> getRolesFromGroup(List<String> roleGroups) throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		List<RoleCode> obj = new ArrayList<>();
		for (KeyValuePair roleObj : domainRef.get(ITEM_AVAILABLE_ROLE)) {
			String groups = roleObj.getAttributes().get(ITEM_AVAILABLE_ROLE__ATTR_GROUPS).toString();
			List<String> groupList = List.of(groups.split(SPLITTER));
			for (String roleGroup : roleGroups) {
				if (groupList.contains(roleGroup)) {
					obj.add(RoleCode.valueOf(roleObj.getKey()));
				}
			}
		}
		return obj.stream().distinct().toList();
	}

	public List<String> getGroupsFromRole(List<RoleCode> codes) throws Exception {
		List<String> obj = new ArrayList<>();
		for (RoleDTO roles : convertToRoleDTO(codes)) {
			obj.addAll(roles.getGroups());
		}
		return obj.stream().distinct().toList();
	}

	/*
	 * *****************************************************************************
	 * ******************* WORKFLOW
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * Get reference data for user
	 * 
	 * @return Map of list of KeyValue objects for user
	 * @throws Exception
	 */
	public RequestDTO convertToWorkflowDTO(WorkflowType type, List<AdditionalField> addifields) throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		KeyValuePair wftype = domainRef.get(ITEM_WORKFLOW_TYPE).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(type.name())).findFirst()
				.orElseThrow(() -> new Exception("No such workflow type found."));

		RequestDTO wf = new RequestDTO();
		wf.setWorkflowName(wftype.getValue());
		String status = wftype.getAttributes().get(ITEM_WORKFLOW_TYPE__ATTR_DEFAULT_STEP).toString();
		wf.setStatus(WorkflowStatus.valueOf(status));
		wf.setType(type);
		if (addifields != null) {
			List<FieldDTO> fieldDTO = new ArrayList<>();
			for (AdditionalField addfield : addifields) {
				fieldDTO.add(findAddtlFieldAndConvertToFieldDTO(AdditionalFieldSource.WORKFLOW, addfield));
			}
			wf.setAdditionalFields(fieldDTO);
		}
		return wf;
	}

	/**
	 * Get reference data for user
	 * 
	 * @param decisionGroup
	 * 
	 * @return Map of list of KeyValue objects for user
	 * @throws Exception
	 */
	public WorkDTO prepareWorkList(WorkflowType type, WorkflowStatus currentStatus, String decisionGroup)
			throws Exception {
		KeyValuePair kvWfStep = getDomainConfig(ITEM_WORKFLOW_STEP).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(currentStatus.name())).findFirst()
				.orElseThrow(() -> new Exception("No such workflow status found."));
		Map<String, Object> attributes = kvWfStep.getAttributes();

		WorkDTO wl = new WorkDTO();
		wl.setWorkflowStatus(currentStatus);
		wl.setWorkflowType(type);
		wl.setCreatedOn(CommonUtils.getSystemDate());
		wl.setDescription(kvWfStep.getDescription());
		wl.setGroupWork(true);
		Object groups = attributes.get(ITEM_WORKFLOW_STEP__ATTR_DECISION_MAKER_ROLE_GROUPS);
		List<String> groupList = new ArrayList<String>();
		if (groups != null) {
			for (String group : groups.toString().split(SPLITTER)) {
				if (!group.equalsIgnoreCase(decisionGroup)) {
					groupList.add(group);
				}
			}
		}
		boolean isDecisionStep = (boolean) attributes.get(ITEM_WORKFLOW_STEP__ATTR_IS_DECISION_STEP);
		if (isDecisionStep) {
			wl.setWorkType(WorkType.DECISION);
		}

		wl.setPendingWithRoleGroups(groupList);
		wl.setPendingWithRoles(this.getRolesFromGroup(groupList));
		boolean isFinalStep = (boolean) attributes.get(ITEM_WORKFLOW_STEP__ATTR_IS_FINAL_STEP);
		wl.setFinalStep(isFinalStep);
		Object actionName = attributes.get(ITEM_WORKFLOW_STEP__ATTR_CURRENT_ACTION + "-" + type.name());
		wl.setCurrentAction(/* isFinalStep || */actionName == null ? WorkFlowAction.NO_ACTION
				: WorkFlowAction.valueOf(actionName.toString()));
		return wl;
	}

	@Deprecated
	public WorkFlowAction getWorkflowAction(WorkflowStatus status, WorkflowType type) throws Exception {
		KeyValuePair kvWfStep = getDomainConfig(ITEM_WORKFLOW_STEP).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(status.name())).findFirst()
				.orElseThrow(() -> new Exception("No such workflow status found."));
		Map<String, Object> attributes = kvWfStep.getAttributes();
		boolean isFinalStep = (boolean) attributes.get(ITEM_WORKFLOW_STEP__ATTR_IS_FINAL_STEP);
		if (isFinalStep) {
			return WorkFlowAction.NO_ACTION;
		}
		Object actionName = attributes.get(ITEM_WORKFLOW_STEP__ATTR_CURRENT_ACTION + "-" + type.name());
		return actionName == null ? WorkFlowAction.NO_ACTION : WorkFlowAction.valueOf(actionName.toString());
	}

	public WorkflowStatus getWorkflowNextStatus(WorkflowStatus status, WorkflowType type, WorkflowDecision decision)
			throws Exception {
		KeyValuePair kvWfStep = getDomainConfig(ITEM_WORKFLOW_STEP).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(status.name())).findFirst()
				.orElseThrow(() -> new Exception("No such workflow status found."));
		Map<String, Object> attributes = kvWfStep.getAttributes();
		boolean isFinalStep = (boolean) attributes.get(ITEM_WORKFLOW_STEP__ATTR_IS_FINAL_STEP);
		if (isFinalStep) {
			return null;
		}
		boolean isDecisionStep = (boolean) attributes.get(ITEM_WORKFLOW_STEP__ATTR_IS_DECISION_STEP);
		Object nextStatus = null;
		if (isDecisionStep) {
			nextStatus = attributes.get(ITEM_WORKFLOW_STEP__ATTR_NEXT_STEP + "-" + type.name() + "-" + decision.name());
		} else {
			nextStatus = attributes.get(ITEM_WORKFLOW_STEP__ATTR_NEXT_STEP + "-" + type.name());
		}

		return nextStatus == null ? null : WorkflowStatus.valueOf(nextStatus.toString());
	}

	/*
	 * *****************************************************************************
	 * ******************* DONATION
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * Get all outstanding donation status
	 * 
	 * @return list of outstanding donation status
	 * @throws Exception
	 */
	public List<DonationStatus> getOutstandingDonationStatus() throws Exception {
		List<KeyValuePair> kvStatus = getDomainConfig(ITEM_DONATION_STATUS);
		List<DonationStatus> outStatus = kvStatus.stream()
				.filter(f -> f.getAttributes().get(ITEM_DONATION_STATUS__ATTR_IS_FINAL_STATUS) != Boolean.TRUE)
				.map(m -> DonationStatus.valueOf(m.getKey())).collect(Collectors.toList());
		return outStatus;
	}

	/**
	 * Checks if donation status resolved or not
	 * 
	 * @param status
	 * @return return true if status is resolved else false
	 * @throws Exception
	 */
	public boolean isResolvedDonation(DonationStatus status) throws Exception {
		List<KeyValuePair> kvStatus = getDomainConfig(ITEM_DONATION_STATUS);
		List<DonationStatus> resolvedStatus = kvStatus.stream()
				.filter(f -> f.getAttributes().get(ITEM_DONATION_STATUS__ATTR_IS_FINAL_STATUS) == Boolean.TRUE)
				.map(m -> DonationStatus.valueOf(m.getKey())).collect(Collectors.toList());
		return resolvedStatus.contains(status);
	}

	public DonationDTO convertToDonationDTO(DonationType type) throws Exception {
		List<KeyValuePair> kvDType = getDomainConfig(ITEM_DONATION_TYPE);
		KeyValuePair donationType = kvDType.stream().filter(f -> f.getKey().equalsIgnoreCase(type.name())).findFirst()
				.orElseThrow(() -> new Exception("Invalid donation type [" + type + "]"));
		Map<String, Object> donTypeAttr = donationType.getAttributes();
		DonationDTO donDTO = new DonationDTO();
		donDTO.setType(type);
		Object lastday = donTypeAttr.get(ITEM_DONATION_TYPE__ATTR_LAST_PAYMENT_DAY);
		if (lastday != null) {
			donDTO.setLastPaymentDay(Integer.parseInt(lastday.toString()));
		}
		Object amount = donTypeAttr.get(ITEM_DONATION_TYPE__ATTR_DEFAULT_AMOUNT);
		if (amount != null) {
			donDTO.setAmount(Double.valueOf(amount.toString()));
		}

		Object status = donTypeAttr.get(ITEM_DONATION_TYPE__ATTR_DEFAULT_STATUS);
		if (status != null) {
			donDTO.setStatus(DonationStatus.valueOf(status.toString()));
		}
		return donDTO;

	}

	/**
	 * Get the next applicable donation status w.r.t. donationType and currentStatus
	 * 
	 * @param donationType
	 * @param currentStatus
	 * @return returns list of statuses
	 * @throws Exception
	 */
	public List<DonationStatus> getNextDonationStatus(DonationType donationType, DonationStatus currentStatus)
			throws Exception {
		List<KeyValuePair> kvStatus = getDomainConfig(ITEM_DONATION_STATUS);
		Optional<KeyValuePair> tarCurrStatus = kvStatus.stream()
				.filter(f -> f.getKey().equalsIgnoreCase(currentStatus.name())).findFirst();
		if (tarCurrStatus.isEmpty()) {
			return List.of();
		}
		String[] nextStatusString = String.valueOf(tarCurrStatus.get().getAttributes()
				.get(ITEM_DONATION_STATUS__ATTR_NEXT_STATUS + "-" + donationType.name())).split(SPLITTER);
		return List.of(nextStatusString).stream().map(m -> DonationStatus.valueOf(m)).collect(Collectors.toList());
	}

	/**
	 * Returns donation reference data Attribute 'nextDonationStatuses' will be
	 * available only if 'donationType' and 'currentStatus' has been specified
	 * 
	 * @param donationType
	 * @param currentStatus
	 * @return Map of list of KeyValue objects for donation
	 * @throws Exception
	 */
	public Map<String, List<KeyValue>> getDonationRefData(DonationType donationType, DonationStatus currentStatus)
			throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		List<KeyValuePair> kvStatus = domainRef.get(ITEM_DONATION_STATUS);
		obj.put("donationStatuses", BusinessObjectConverter.toKeyValueList(kvStatus));
		obj.put("donationTypes", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_DONATION_TYPE)));
		obj.put("paymentMethods", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_PAYMENT_METHODS)));
		obj.put("upiOptions", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_UPI_OPTIONS)));
		if (donationType != null && currentStatus != null) {
			Optional<KeyValuePair> tarCurrStatus = kvStatus.stream()
					.filter(f -> f.getKey().equalsIgnoreCase(currentStatus.name())).findFirst();
			if (!tarCurrStatus.isEmpty()) {
				String[] nextStatusString = String.valueOf(tarCurrStatus.get().getAttributes()
						.get(ITEM_DONATION_STATUS__ATTR_NEXT_STATUS + donationType.name())).split(SPLITTER);
				List<KeyValue> nextStatus = List.of(nextStatusString).stream().filter(f -> !StringUtils.isEmpty(f))
						.map(m -> {
							KeyValue kv = new KeyValue();
							kv.setKey(m);
							Optional<KeyValuePair> dispValue = kvStatus.stream()
									.filter(f -> f.getKey().equalsIgnoreCase(m)).findFirst();
							kv.setValue(dispValue.isEmpty() ? null : dispValue.get().getValue());
							return kv;
						}).collect(Collectors.toList());
				obj.put("nextDonationStatuses", nextStatus);
			}
		}
		return obj;
	}

	/*
	 * *****************************************************************************
	 * ******************* ADDITIONAL FIELDS
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * This method will find additional field for donation and convert it to
	 * FieldDTO
	 * 
	 * @param additionalField
	 * @return returns instance of FieldDTO
	 * @throws Exception if no value found for the key
	 */
	public FieldDTO findAddtlFieldAndConvertToFieldDTO(AdditionalFieldSource sourceType,
			AdditionalField additionalField) throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_ADDITIONAL_FIELDS);
		KeyValuePair field = kvFields.stream()
				.filter(f -> f.getKey().equalsIgnoreCase(additionalField.getKey().name())
						&& sourceType.name().equalsIgnoreCase(f.getDescription()))
				.findFirst().orElseThrow(() -> new Exception("Invalid additional key"));
		FieldDTO fieldDTO = new FieldDTO();
		fieldDTO.setFieldId(additionalField.getId());
		fieldDTO.setFieldName(field.getValue());
		fieldDTO.setFieldType(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_TYPE)));
		fieldDTO.setFieldKey(additionalField.getKey());
		fieldDTO.setFieldValue(additionalField.getValue());
		fieldDTO.setHidden(additionalField.isHidden());
		fieldDTO.setEncrypted(additionalField.isEncrypted());
		fieldDTO.setFieldSourceType(sourceType);
		return fieldDTO;
	}

	/*
	 * *****************************************************************************
	 * ******************* PASSWORD POLICY
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * Get password policy description for a specific policy
	 * 
	 * @param passwordPolicy
	 * @return password policy description
	 * @throws Exception
	 */
	public String getPasswordPolicyDescription(String passwordPolicy) throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_PASSWORD_COMPLEXITY_OPTIONS);

		KeyValuePair field = kvFields.stream().filter(f -> f.getKey().equalsIgnoreCase(passwordPolicy)).findFirst()
				.orElseThrow(() -> new Exception("Invalid passwordPolicy [" + passwordPolicy + "]"));
		return field.getDescription();
	}

	/**
	 * Get password policy regex for a specific policy
	 * 
	 * @param passwordPolicy
	 * @return password policy regex
	 * @throws Exception
	 */
	public String getPasswordPolicyRegex(String passwordPolicy) throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_PASSWORD_COMPLEXITY_OPTIONS);
		KeyValuePair field = kvFields.stream().filter(f -> f.getKey().equalsIgnoreCase(passwordPolicy)).findFirst()
				.orElseThrow(() -> new Exception("Invalid passwordPolicy [" + passwordPolicy + "]"));
		return field.getValue();
	}

	/**
	 * get all basic info about Nabarun as list of KeyValue pair
	 * 
	 * @return list of KeyValue pair
	 * @throws Exception
	 */
	public List<KeyValue> getNabarunOrgInfo() throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_NABARUN_ORG_INFO);
		return BusinessObjectConverter.toKeyValueList(kvFields);
	}

	/*
	 * *****************************************************************************
	 * ******************* RULES AND REGULATION
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * Get list of all rules and regulation
	 * 
	 * @return list of KeyValue pair
	 * @throws Exception
	 */
	public List<KeyValue> getRules() throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_NABARUN_RULES_REGULATIONS);
		return BusinessObjectConverter.toKeyValueList(kvFields);
	}

	/*
	 * *****************************************************************************
	 * ******************* EXCEPTION MESSAGES
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * Throw exception if business condition is met
	 * 
	 * @param condition
	 * @param name
	 * @param code
	 * @throws Exception
	 */
	protected void throwBusinessExceptionIf(BusinessCondition condition, ExceptionEvent name, String code)
			throws Exception {
		if (condition.get()) {
			List<KeyValuePair> kvFields = getDomainConfig(ITEM_BUSINESS_EXCEPTION_MESSAGES);
			String message = kvFields.stream().filter(f -> f.getKey().equalsIgnoreCase(name.name())).findFirst()
					.map(m -> m.getValue()).orElseGet(() -> "Something went wrong. Please try again later.");
			throw new BusinessException(message + (code == null ? "" : "[" + code + "]"));
		}
	}

	/**
	 * Throw exception if business condition is met
	 * 
	 * @param condition
	 * @param name
	 * @throws Exception
	 */
	public void throwBusinessExceptionIf(BusinessCondition condition, ExceptionEvent name) throws Exception {
		throwBusinessExceptionIf(condition, name, null);
	}

	/*
	 * *****************************************************************************
	 * ******************* EMAIL TEMPLATE
	 * *****************************************************************************
	 * *******************
	 */

	/**
	 * This method will first find email template then Interpolation of string
	 * template is done Then convert it to EmailTemplateDTO using BeanWrapper
	 * 
	 * @param name
	 * @param objectMap
	 * @return
	 * @throws Exception
	 */
	public EmailTemplateDTO findInterpolateAndConvertToEmailTemplateDTO(String name, Map<String, Object> objectMap)
			throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_EMAIL_TEMPLATE_CONFIG);
		KeyValuePair template = kvFields.stream().filter(f -> f.getKey().equalsIgnoreCase(name)).findFirst()
				.orElseThrow(() -> new Exception("No template found [" + name + "]"));
		Map<String, Object> attributes = template.getAttributes();
		for (String key : attributes.keySet()) {
			if (attributes.get(key) != null) {
				String text = attributes.get(key).toString();
				/*
				 * String Interpolation
				 */
				ST st = new ST(text);
				for (String object : objectMap.keySet()) {
					st.add(object, objectMap.get(object));
				}
				attributes.put(key, st.render());
			}
		}
		EmailTemplateDTO emailTemplate = new EmailTemplateDTO();
		BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(emailTemplate);
		wrapper.setAutoGrowNestedPaths(true);
		wrapper.setPropertyValues(attributes);
		emailTemplate.setTemplateId(template.getValue());
		return emailTemplate;
	}

	private String interpolateText(String text, Map<String, Object> objectMap) {
		ST st = new ST(text);
		for (String object : objectMap.keySet()) {
			st.add(object, objectMap.get(object));
		}
		return st.render();
	}

	public NotificationDTO findInterpolateAndConvertToNotificationDTO(String templateName,
			Map<String, Object> objectMap) throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_NOTIFICATION_TEMPLATE_CONFIG);
		KeyValuePair template = kvFields.stream().filter(f -> f.getKey().equalsIgnoreCase(templateName)).findFirst()
				.orElseThrow(() -> new Exception("No template found [" + templateName + "]"));

		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setTitle(interpolateText(template.getValue(), objectMap));
		notificationDTO.setSummary(interpolateText(template.getDescription(), objectMap));

		Map<String, Object> attributes = template.getAttributes();
		for (String key : attributes.keySet()) {
			if (attributes.get(key) != null) {
				String text = attributes.get(key).toString();
				attributes.put(key, interpolateText(text, objectMap));
			}
		}
		notificationDTO.setCommand(attributes.get("ACTION") == null ? null : attributes.get("ACTION").toString());
		// notificationDTO.setExtLink(attributes.get("extLink") == null ? null :
		// attributes.get("extLink").toString());
		notificationDTO.setImage(attributes.get("IMAGE_URL") == null ? null : attributes.get("IMAGE_URL").toString());
		notificationDTO
				.setLink(attributes.get("ACTION_LINK") == null ? null : attributes.get("ACTION_LINK").toString());
		notificationDTO.setType(attributes.get("TYPE") == null ? NotificationType.FYI
				: NotificationType.valueOf(attributes.get("TYPE").toString()));
		return notificationDTO;
	}

}
