package ngo.nabarun.app.businesslogic.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.LinkCategoryDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessCondition;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.common.annotation.NoLogging;
import ngo.nabarun.app.common.enums.AdditionalConfigKey;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.LoginMethod;
import ngo.nabarun.app.common.enums.NotificationType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.WorkAction;
import ngo.nabarun.app.common.enums.WorkDecision;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.RequestStatus;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO.EmailBodyTemplate.TableTemplate;
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
	// private static Map<String, List<KeyValuePair>> domainConfig = null;
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
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_TYPE = "FIELD_TYPE";
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_VALUE_TYPE = "TYPE";
	private static final String ITEM_AVAILABLE_ROLE__ATTR_GROUPS = "GROUP";

	private static final String ITEM_WORKFLOW_TYPES = "WORKFLOW_TYPES";
	private static final String ITEM_WORKFLOW_TYPES__ATTR_DEFAULT_STEP = "DEFAULT_STEP";
	private static final String ITEM_COMMON__ATTR_IS_VISIBLE = "IS_VISIBLE";

	private static final String ITEM_ADDITIONAL_CONFIG = "ADDITIONAL_CONFIG";
	private static final String ITEM_WORKFLOW_STEPS = "WORKFLOW_STEPS";
	private static final String ITEM_WORKFLOW_STEPS__ATTR_IS_FINAL_STEP = "IS_FINAL_STEP";
	private static final String ITEM_WORKFLOW_STEPS__ATTR_CURRENT_ACTION = "CURRENT_ACTION";
	private static final String ITEM_WORKFLOW_STEPS__ATTR_STEP_TYPE = "STEP_TYPE";
	private static final String ITEM_WORKFLOW_STEPS__ATTR_NEXT_STEP = "NEXT_STEP";
	private static final String ITEM_WORKFLOW_STEPS__ATTR_DECISION_MAKER_ROLE_GROUPS = "DECISION_MAKER_ROLE_GROUPS";
	private static final String ITEM_WORKFLOW_STEPS__ATTR_APPLICABLE_FOR = "APPLICABLE_FOR";

	private static final String ITEM_ACCOUNT_TYPE = "ACCOUNT_TYPES";
	private static final String ITEM_ACCOUNT_STATUS = "ACCOUNT_STATUSES";
	private static final String ITEM_EXPENSE_STATUS = "EXPENSE_STATUSES";
	private static final String ITEM_TRANSACTION_TYPE = "TRANSACTION_TYPES";
	private static final String ITEM_PROFILE_STATUSES = "PROFILE_STATUSES";
	private static final String ITEM_COUNTRY_LIST = "COUNTRY_LIST";
	private static final String ITEM_STATE_LIST = "STATE_LIST";
	private static final String ITEM_DISTRICT_LIST = "DISTRICT_LIST";
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_APPLICABLE_FOR = "APPLICABLE_FOR";
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_MANDATORY = "MANDATORY";
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_OPTIONS = "FIELD_OPTIONS";
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_HIDDEN = "HIDDEN";
	private static final String ITEM_ADDITIONAL_FIELDS__ATTR_ENCRYPTED = "ENCRYPTED";
	private static final String ITEM_WORKFLOW_TYPES__ATTR_SYSTEM_GENERATED = "SYSTEM_GENERATED";
	private static final String ITEM_USER_CONNECTIONS = "USER_CONNECTIONS";
	private static final String ITEM_IMPORTANT_LINKS = "IMPORTANT_LINKS";
	private static final String ITEM_POLICY_LINKS = "POLICY_LINKS";
	private static final String ITEM_POLICY_LINKS__ATTR_CATEGORY = "CATEGORY";
	private static final String ITEM_USER_GUIDE_LINKS = "USER_GUIDE_LINKS";

	protected Map<String, List<KeyValuePair>> getDomainConfigs() throws Exception {
		return domainInfraService.getDomainRefConfigs();
	}

	protected Map<String, List<KeyValuePair>> getDomainLocation() throws Exception {
		return domainInfraService.getDomainLocationData();
	}

	public String getDisplayValue(String key) throws Exception {
		return getDomainKeyValues().get(key);
	}

	public Map<String, String> getDomainKeyValues() throws Exception {
		if (domainKeyValue.isEmpty()) {
			Map<String, List<KeyValuePair>> configs = getDomainConfigs();
			for (Entry<String, List<KeyValuePair>> config : configs.entrySet()) {
				for (KeyValuePair item : config.getValue()) {
					domainKeyValue.put(item.getKey(), item.getValue());
				}
			}
			Map<String, List<KeyValuePair>> locations = getDomainLocation();
			for (Entry<String, List<KeyValuePair>> location : locations.entrySet()) {
				for (KeyValuePair item : location.getValue()) {
					domainKeyValue.put(item.getKey(), item.getValue());
				}
			}
		}
		// System.err.println(domainKeyValue);
		return domainKeyValue;
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
	public boolean isTitleGenderAligned(String title, String gender) {
		List<KeyValuePair> kvTitle = null;
		try {
			kvTitle = getDomainConfig(ITEM_USER_TITLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (kvTitle == null) {
			return false;
		}
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
	@NoLogging
	public Map<String, List<KeyValue>> getUserRefData(String countryCode, String stateCode) throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		Map<String, List<KeyValuePair>> locationRef = domainInfraService.getDomainLocationData();
		obj.put("userTitles", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_USER_TITLE)));
		obj.put("userGenders", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_USER_GENDER)));
		obj.put("availableRoles", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_AVAILABLE_ROLE)));
		obj.put("availableRoleGroups",
				BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_AVAILABLE_ROLE_GROUP)));
		obj.put("userStatuses",
				BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_PROFILE_STATUSES).stream()
						.filter(m -> m.getAttributes().get(ITEM_COMMON__ATTR_IS_VISIBLE) == Boolean.TRUE)
						.collect(Collectors.toList())));

		List<KeyValuePair> states = locationRef.get(ITEM_STATE_LIST);
		if (countryCode != null) {
			states = states.stream().filter(f -> countryCode.equals(f.getAttributes().get("COUNTRYKEY")))
					.collect(Collectors.toList());
		}
		List<KeyValuePair> districts = locationRef.get(ITEM_DISTRICT_LIST);
		if (countryCode != null && stateCode != null) {
			districts = districts.stream().filter(f -> countryCode.equals(f.getAttributes().get("COUNTRYKEY"))
					&& stateCode.equals(f.getAttributes().get("STATEKEY"))).collect(Collectors.toList());
		}

		List<KeyValuePair> connections = domainRef.get(ITEM_USER_CONNECTIONS);
		obj.put("countries", BusinessObjectConverter.toKeyValueList(locationRef.get(ITEM_COUNTRY_LIST)));
		obj.put("phoneCodes", BusinessObjectConverter.toKeyValueList(locationRef.get(ITEM_COUNTRY_LIST), "DIALCODE"));
		obj.put("districts", BusinessObjectConverter.toKeyValueList(districts));
		obj.put("states", BusinessObjectConverter.toKeyValueList(states));
		obj.put("loginMethods", BusinessObjectConverter.toKeyValueList(connections));

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

	public RoleDTO convertToRoleDTO(RoleCode roleCode) throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		for (KeyValuePair roleObj : domainRef.get(ITEM_AVAILABLE_ROLE)) {
			if (roleCode == RoleCode.valueOf(roleObj.getKey().trim())) {
				RoleDTO role = new RoleDTO();
				role.setCode(RoleCode.valueOf(roleObj.getKey()));
				role.setDescription(roleObj.getDescription());
				String groups = roleObj.getAttributes().get(ITEM_AVAILABLE_ROLE__ATTR_GROUPS).toString();
				// role.setGroups(List.of(groups.split(",")).stream().map(m->RoleGroup.valueOf(m)).toList());
				role.setGroups(List.of(groups.split(SPLITTER)));
				role.setName(roleObj.getValue());
				return role;
			}
		}
		return null;
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
	public RequestDTO convertToRequestDTO(RequestType type, List<FieldDTO> addifields) throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		KeyValuePair wftype = domainRef.get(ITEM_WORKFLOW_TYPES).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(type.name())).findFirst()
				.orElseThrow(() -> new Exception("No such request type found."));

		RequestDTO wf = new RequestDTO();
		wf.setWorkflowName(wftype.getValue());
		String status = wftype.getAttributes().get(ITEM_WORKFLOW_TYPES__ATTR_DEFAULT_STEP).toString();
		wf.setStatus(RequestStatus.valueOf(status));
		wf.setDescription(wftype.getDescription());
		wf.setType(type);
		if (addifields != null) {
			List<FieldDTO> fieldDTO = new ArrayList<>();
			for (FieldDTO addfield : addifields) {
				fieldDTO.add(findAddtlFieldAndConvertToFieldDTO("REQUEST-" + type.name(), addfield));
			}
			wf.setAdditionalFields(fieldDTO);
		}
		Object sysGen = wftype.getAttributes().get(ITEM_WORKFLOW_TYPES__ATTR_SYSTEM_GENERATED);

		wf.setSystemGenerated(sysGen == null ? false : Boolean.valueOf(sysGen.toString()));
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
	public WorkDTO prepareWorkList(RequestType type, RequestStatus workItem, String decisionGroup) throws Exception {
		KeyValuePair kvWfStep = getDomainConfig(ITEM_WORKFLOW_STEPS).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(workItem.name())).findFirst()
				.orElseThrow(() -> new Exception("No such workitem found."));
		Map<String, Object> attributes = kvWfStep.getAttributes();

		WorkDTO wl = new WorkDTO();
		wl.setWorkSourceStatus(workItem);
		wl.setWorkSourceType(type);
		wl.setCreatedOn(CommonUtils.getSystemDate());
		wl.setDescription(kvWfStep.getDescription());
		wl.setGroupWork(true);
		Object groups = attributes.get(ITEM_WORKFLOW_STEPS__ATTR_DECISION_MAKER_ROLE_GROUPS);
		List<String> groupList = new ArrayList<String>();
		if (groups != null) {
			for (String group : groups.toString().split(SPLITTER)) {
				if (!group.equalsIgnoreCase(decisionGroup)) {
					groupList.add(group);
				}
			}
		}
		Object stepType = attributes.get(ITEM_WORKFLOW_STEPS__ATTR_STEP_TYPE);
		if (stepType != null) {
			wl.setWorkType(WorkType.valueOf(stepType.toString()));
		}

		wl.setPendingWithRoleGroups(groupList);
		wl.setPendingWithRoles(this.getRolesFromGroup(groupList));
		boolean isFinalStep = (boolean) attributes.get(ITEM_WORKFLOW_STEPS__ATTR_IS_FINAL_STEP);
		wl.setFinalStep(isFinalStep);
		Object actionName = attributes.get(ITEM_WORKFLOW_STEPS__ATTR_CURRENT_ACTION + "-" + type.name());
		wl.setCurrentAction(/* isFinalStep || */actionName == null ? WorkAction.NO_ACTION
				: WorkAction.valueOf(actionName.toString()));
		return wl;
	}

	@Deprecated
	public WorkAction getWorkflowAction(RequestStatus status, RequestType type) throws Exception {
		KeyValuePair kvWfStep = getDomainConfig(ITEM_WORKFLOW_STEPS).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(status.name())).findFirst()
				.orElseThrow(() -> new Exception("No such workflow status found."));
		Map<String, Object> attributes = kvWfStep.getAttributes();
		boolean isFinalStep = (boolean) attributes.get(ITEM_WORKFLOW_STEPS__ATTR_IS_FINAL_STEP);
		if (isFinalStep) {
			return WorkAction.NO_ACTION;
		}
		Object actionName = attributes.get(ITEM_WORKFLOW_STEPS__ATTR_CURRENT_ACTION + "-" + type.name());
		return actionName == null ? WorkAction.NO_ACTION : WorkAction.valueOf(actionName.toString());
	}

	public RequestStatus getWorkflowNextStatus(RequestStatus status, RequestType type, String decision)
			throws Exception {
		KeyValuePair kvWfStep = getDomainConfig(ITEM_WORKFLOW_STEPS).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(status.name())).findFirst()
				.orElseThrow(() -> new Exception("No such workflow status found."));
		Map<String, Object> attributes = kvWfStep.getAttributes();
		boolean isFinalStep = (boolean) attributes.get(ITEM_WORKFLOW_STEPS__ATTR_IS_FINAL_STEP);
		if (isFinalStep) {
			return null;
		}
		Object stepType = attributes.get(ITEM_WORKFLOW_STEPS__ATTR_STEP_TYPE);
		Object nextStatus = null;
		if (stepType != null && (WorkType.valueOf(stepType.toString()) == WorkType.DECISION
				|| WorkType.valueOf(stepType.toString()) == WorkType.CONFIRMATION)) {
			nextStatus = attributes.get(ITEM_WORKFLOW_STEPS__ATTR_NEXT_STEP + "-" + type.name() + "-" + decision);
		} else {
			nextStatus = attributes.get(ITEM_WORKFLOW_STEPS__ATTR_NEXT_STEP + "-" + type.name());
		}

		return nextStatus == null ? null : RequestStatus.valueOf(nextStatus.toString());
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

	public DonationDTO convertToDonationDTO(DonationDetail donationDetail) throws Exception {
		DonationType type = donationDetail.getDonationType();
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

		/**
		 * Custom field
		 */
		if (donationDetail.getAdditionalFields() != null) {
			List<FieldDTO> fieldDTO = new ArrayList<>();
			List<FieldDTO> fieldList = BusinessObjectConverter.toFieldDTO(donationDetail.getAdditionalFields());
			for (FieldDTO addfield : fieldList) {
				fieldDTO.add(findAddtlFieldAndConvertToFieldDTO("DONATION-" + type.name(), addfield));
			}
			donDTO.setAdditionalFields(fieldDTO);
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
				String[] nextStatusString = String
						.valueOf(tarCurrStatus.get().getAttributes()
								.get(ITEM_DONATION_STATUS__ATTR_NEXT_STATUS + "-" + donationType.name()))
						.split(SPLITTER);
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
	public FieldDTO findAddtlFieldAndConvertToFieldDTO(String sourceType, FieldDTO additionalField) throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_ADDITIONAL_FIELDS);
		KeyValuePair field = kvFields.stream().filter(f -> {
			List<String> applicable_for = List.of(
					String.valueOf(f.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_APPLICABLE_FOR)).split(SPLITTER));
			return f.getKey().equalsIgnoreCase(additionalField.getFieldKey().name())
					&& applicable_for.contains(sourceType);
		}).findFirst()
				.orElseThrow(() -> new Exception("Invalid additional key '" + additionalField.getFieldKey().name()));
		FieldDTO fieldDTO = new FieldDTO();
		fieldDTO.setFieldId(additionalField.getFieldId());
		fieldDTO.setFieldName(field.getValue());
		fieldDTO.setFieldType(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_TYPE)));
		fieldDTO.setFieldKey(additionalField.getFieldKey());
		fieldDTO.setFieldValue(additionalField.getFieldValue());
		fieldDTO.setHidden(
				Boolean.valueOf(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_HIDDEN))));
		fieldDTO.setEncrypted(
				Boolean.valueOf(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_ENCRYPTED))));
		fieldDTO.setFieldSourceType(sourceType);
		fieldDTO.setMandatory(
				Boolean.valueOf(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_MANDATORY))));
		fieldDTO.setFieldOptions(List.of(
				String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_OPTIONS)).split(SPLITTER)));
		fieldDTO.setFieldValueType(
				String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_VALUE_TYPE)));
		return fieldDTO;
	}

	private List<KeyValuePair> getAdditionalFields(String sourceType) throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_ADDITIONAL_FIELDS);
		List<KeyValuePair> fields = kvFields.stream().filter(f -> {
			List<String> applicable_for = List.of(
					String.valueOf(f.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_APPLICABLE_FOR)).split(SPLITTER));
			return applicable_for.contains(sourceType);
		}).collect(Collectors.toList());
		return fields;
	}

	public List<FieldDTO> findAddtlFieldDTOList(String sourceType) throws Exception {
		List<FieldDTO> fields = getAdditionalFields(sourceType).stream().map(field -> {
			FieldDTO fieldDTO = new FieldDTO();
			fieldDTO.setFieldName(field.getValue());
			fieldDTO.setFieldType(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_TYPE)));
			fieldDTO.setFieldKey(AdditionalFieldKey.valueOf(field.getKey()));
			fieldDTO.setFieldSourceType(sourceType);
			fieldDTO.setMandatory(
					Boolean.valueOf(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_MANDATORY))));
			fieldDTO.setFieldOptions(List.of(String
					.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_OPTIONS)).split(SPLITTER)));
			fieldDTO.setFieldValueType(
					String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_FIELD_VALUE_TYPE)));
			fieldDTO.setHidden(
					Boolean.valueOf(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_HIDDEN))));
			fieldDTO.setEncrypted(
					Boolean.valueOf(String.valueOf(field.getAttributes().get(ITEM_ADDITIONAL_FIELDS__ATTR_ENCRYPTED))));
			return fieldDTO;
		}).collect(Collectors.toList());

		return fields;
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

	public List<KeyValue> getPolicyDocs() throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_POLICY_LINKS);
		return BusinessObjectConverter.toKeyValueList(kvFields);
	}

	public List<KeyValue> getUserGuideDocs() throws Exception {
		List<KeyValuePair> kvFields = getDomainConfig(ITEM_USER_GUIDE_LINKS);
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
		// System.err.println(objectMap);
		Map<String, Object> attributes = template.getAttributes();
		for (String key : attributes.keySet()) {
			if (attributes.get(key) != null) {
				String text = attributes.get(key).toString();
				/*
				 * String Interpolation
				 */
				try {
					ST st = new ST(text);
					for (String object : objectMap.keySet()) {
						st.add(object, objectMap.get(object));
					}
					attributes.put(key, st.render());
				} catch (Exception e) {
					System.err.println(e + " " + text);
				}

			}
		}

		EmailTemplateDTO emailTemplate = new EmailTemplateDTO();
		BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(emailTemplate);
		wrapper.setAutoGrowNestedPaths(true);
		wrapper.setPropertyValues(attributes);
		emailTemplate.setTemplateId(template.getValue());
		if (emailTemplate.getBody() != null && emailTemplate.getBody().getContent() != null
				&& emailTemplate.getBody().getContent().getTable() != null) {
			List<TableTemplate> template_1 = new ArrayList<>();
			for (TableTemplate table : emailTemplate.getBody().getContent().getTable()) {
				if (table.getDataString() != null) {
					table.setData(convertRenderedStringTo2DArray(table.getDataString()));
					template_1.add(table);
				}
			}
			emailTemplate.getBody().getContent().setTable(template_1);
		}
		return emailTemplate;
	}

	private static String[][] convertRenderedStringTo2DArray(String input) {
		// Step 1: Clean up the input by removing the outer brackets
		String cleanedInput = input.substring(1, input.length() - 1); // Remove outer [ and ]

		// Step 2: Split the input into individual rows
		String[] rows = cleanedInput.split("\\], \\[");

		// Step 3: Create the 2D array to hold the result
		String[][] result = new String[rows.length][];

		// Step 4: Process each row using regex to handle commas within quotes
		Pattern pattern = Pattern.compile("\"([^\"]*)\""); // Match text inside double quotes
		for (int i = 0; i < rows.length; i++) {
			// Find all matches in the row
			Matcher matcher = pattern.matcher(rows[i]);
			List<String> elements = new ArrayList<>();

			// Extract each quoted element
			while (matcher.find()) {
				elements.add(matcher.group(1)); // Add matched content (without quotes)
			}

			// Convert the list of elements to an array
			result[i] = elements.toArray(new String[0]);
		}
		return result;
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

	public Map<String, List<KeyValue>> getAccountRefData() throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		obj.put("accountTypes", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_ACCOUNT_TYPE)));
		obj.put("accountStatuses", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_ACCOUNT_STATUS)));
		obj.put("expenseStatuses", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_EXPENSE_STATUS)));
		obj.put("transactionRefTypes", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_TRANSACTION_TYPE)));
		return obj;
	}

	public Map<String, List<KeyValue>> getAdminRefData() throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		obj.put("importantLinks", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_IMPORTANT_LINKS)));
		return obj;
	}

	public Map<String, List<KeyValue>> getWorkflowRefData(RequestType workflowType) throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		obj.put("workflowTypes", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_WORKFLOW_TYPES)));
		List<KeyValuePair> visibleWFTypeKV = domainRef.get(ITEM_WORKFLOW_TYPES).stream().filter(f -> {
			Object isVisible = f.getAttributes().get(ITEM_COMMON__ATTR_IS_VISIBLE);
			return isVisible != null && Boolean.valueOf(isVisible.toString());
		}).collect(Collectors.toList());
		obj.put("visibleWorkflowTypes", BusinessObjectConverter.toKeyValueList(visibleWFTypeKV));
		obj.put("workflowSteps", BusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_WORKFLOW_STEPS)));
		if (workflowType != null) {
			List<KeyValuePair> applicableWFStepsKV = domainRef.get(ITEM_WORKFLOW_STEPS).stream().filter(f -> {
				Object applicable_for = f.getAttributes().get(ITEM_WORKFLOW_STEPS__ATTR_APPLICABLE_FOR);
				return applicable_for != null && workflowType.name().equalsIgnoreCase(applicable_for.toString());
			}).collect(Collectors.toList());
			obj.put("applicableWorkflowSteps", BusinessObjectConverter.toKeyValueList(applicableWFStepsKV));
		}
		List<KeyValue> workType = new ArrayList<>();
		for (WorkType type : WorkType.values()) {
			KeyValue kv = new KeyValue();
			kv.setKey(type.name());
			kv.setValue(type.getName());
			workType.add(kv);
		}
		List<KeyValue> workDecision = new ArrayList<>();
		for (WorkDecision type : WorkDecision.values()) {
			KeyValue kv = new KeyValue();
			kv.setKey(type.name());
			kv.setValue(type.getValue());
			workDecision.add(kv);
		}
		obj.put("workType", workType);
		obj.put("workDecision", workDecision);
		return obj;
	}

	public List<LoginMethod> getAvailableLoginMethods() throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		return domainRef.get(ITEM_USER_CONNECTIONS).stream().map(m -> LoginMethod.valueOf(m.getKey()))
				.collect(Collectors.toList());
	}

	public List<LinkCategoryDetail> getLinks(String name) throws Exception {
		Map<String, List<KeyValuePair>> domainRef = getDomainConfigs();
		List<KeyValuePair> policyLinks = domainRef.get(name);
		Map<String, List<KeyValuePair>> grouped = new HashMap<>();
		for (KeyValuePair kv : policyLinks) {
			Object catObj = kv.getAttributes().get(ITEM_POLICY_LINKS__ATTR_CATEGORY);
			String category = catObj == null ? "Others" : catObj.toString();
			grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(kv);
		}
		List<LinkCategoryDetail> linkCache = new ArrayList<>();
		for (String category : grouped.keySet()) {
			LinkCategoryDetail categoryDetail = new LinkCategoryDetail();
			categoryDetail.setName(category);
			categoryDetail.setDocuments(BusinessObjectConverter.toKeyValueList(grouped.get(category)));
			linkCache.add(categoryDetail);
		}
		return linkCache;
	}

	public List<LinkCategoryDetail> getPolicyLinks() throws Exception {
		return getLinks(ITEM_POLICY_LINKS);
	}

	public List<LinkCategoryDetail> getUserGuideLinks() throws Exception {
		return getLinks(ITEM_USER_GUIDE_LINKS);
	}

}
