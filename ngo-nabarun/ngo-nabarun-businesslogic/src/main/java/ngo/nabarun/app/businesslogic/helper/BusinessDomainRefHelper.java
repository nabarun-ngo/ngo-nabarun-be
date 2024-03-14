package ngo.nabarun.app.businesslogic.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;
import ngo.nabarun.app.infra.service.IGlobalDataInfraService;

@Service
public class BusinessDomainRefHelper {

	@Autowired
	private IGlobalDataInfraService domainInfraService;

	private static final String SPLITTER = ",";
	private static final String STAR = "*";

	/**
	 * <--------------------- USER HELPERS STARTS FROM HERE------------------>
	 */
	private static final String ITEM_USER_TITLE = "USER_TITLES";
	private static final String ITEM_USER_GENDER = "USER_GENDERS";
	private static final String ITEM_AVAILABLE_ROLE = "USER_ROLES";
	private static final String ITEM_AVAILABLE_ROLE_GROUP = "USER_ROLE_GROUPS";
	private static final String ATTR_GENDER = "GENDER";

	public boolean isTitleGenderAligned(String title, String gender) throws Exception {
		List<KeyValuePair> kvTitle = domainInfraService.getDomainRefConfigs().get(ITEM_USER_TITLE);

		Optional<KeyValuePair> userTitle = kvTitle.stream().filter(f -> f.getKey().equalsIgnoreCase(title)).findFirst();
		if (userTitle.isEmpty()) {
			return false;
		}
		String allowedGender = String.valueOf(userTitle.get().getAttributes().get(ATTR_GENDER));
		return allowedGender.contains(STAR) || allowedGender.toUpperCase().contains(gender.toUpperCase());
	}

	public String getGenderValue(String key) throws Exception {
		List<KeyValuePair> kvGender = domainInfraService.getDomainRefConfigs().get(ITEM_USER_GENDER);
		Optional<KeyValuePair> gender = kvGender.stream().filter(f -> f.getKey().equalsIgnoreCase(key)).findFirst();
		return gender.isEmpty() ? key : gender.get().getValue();
	}

	public String getTitleValue(String titleKey) throws Exception {
		Optional<KeyValuePair> title = domainInfraService.getDomainRefConfigs().get(ITEM_USER_TITLE).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(titleKey)).findFirst();
		return title.isEmpty() ? titleKey : title.get().getValue();
	}

	public Map<String, List<KeyValue>> getUserRefData() throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = domainInfraService.getDomainRefConfigs();
		obj.put("userTitles", DTOToBusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_USER_TITLE)));
		obj.put("userGenders", DTOToBusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_USER_GENDER)));
		obj.put("availableRoles", DTOToBusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_AVAILABLE_ROLE)));
		obj.put("availableRoleGroups",
				DTOToBusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_AVAILABLE_ROLE_GROUP)));
		return obj;
	}

	/**
	 * <------------------ USER HELPERS ENDS HERE--------------------- >
	 * <------------------ ROLES HELPERS STARTS FROM HERE --------------------->
	 */
	public boolean isSameRoleGroup(String roleCode) {
		return false;
	}

	/**
	 * <-------------------ROLES HELPERS ENDS HERE---------------------- >
	 * <------------------ DONATION HELPERS STARTS FROM HERE--------------------->
	 */
	private static final String ITEM_DONATION_STATUS = "DONATION_STATUSES";
	private static final String ATTR_IS_FINAL_STATUS = "IS_FINAL_STATUS";
	private static final String ATTR_NEXT_STATUS = "NEXT_STATUS-";
	private static final String ITEM_DONATION_TYPE = "DONATION_TYPES";
	private static final String ITEM_PAYMENT_METHODS = "PAYMENT_METHODS";
	private static final String ITEM_UPI_OPTIONS = "UPI_OPTIONS";

	public List<DonationStatus> getOutstandingDonationStatus() throws Exception {
		List<KeyValuePair> kvStatus = domainInfraService.getDomainRefConfigs().get(ITEM_DONATION_STATUS);
		List<DonationStatus> outStatus = kvStatus.stream()
				.filter(f -> f.getAttributes().get(ATTR_IS_FINAL_STATUS) != Boolean.TRUE)
				.map(m -> DonationStatus.valueOf(m.getKey())).collect(Collectors.toList());
		return outStatus;
	}

	public boolean isResolvedDonation(DonationStatus status) throws Exception {
		List<KeyValuePair> kvStatus = domainInfraService.getDomainRefConfigs().get(ITEM_DONATION_STATUS);
		List<DonationStatus> resolvedStatus = kvStatus.stream()
				.filter(f -> f.getAttributes().get(ATTR_IS_FINAL_STATUS) == Boolean.TRUE)
				.map(m -> DonationStatus.valueOf(m.getKey())).collect(Collectors.toList());
		return resolvedStatus.contains(status);
	}

	public List<DonationStatus> getNextDonationStatus(DonationType donationType, DonationStatus currentStatus)
			throws Exception {
		List<KeyValuePair> kvStatus = domainInfraService.getDomainRefConfigs().get(ITEM_DONATION_STATUS);
		Optional<KeyValuePair> tarCurrStatus = kvStatus.stream()
				.filter(f -> f.getKey().equalsIgnoreCase(currentStatus.name())).findFirst();
		if (tarCurrStatus.isEmpty()) {
			return List.of();
		}
		String[] nextStatusString = String
				.valueOf(tarCurrStatus.get().getAttributes().get(ATTR_NEXT_STATUS + donationType.name()))
				.split(SPLITTER);
		return List.of(nextStatusString).stream().map(m -> DonationStatus.valueOf(m)).collect(Collectors.toList());
	}

	public Map<String, List<KeyValue>> getDonationRefData(DonationType donationType, DonationStatus currentStatus)
			throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		Map<String, List<KeyValuePair>> domainRef = domainInfraService.getDomainRefConfigs();
		List<KeyValuePair> kvStatus = domainInfraService.getDomainRefConfigs().get(ITEM_DONATION_STATUS);
		obj.put("donationStatuses", DTOToBusinessObjectConverter.toKeyValueList(kvStatus));
		obj.put("donationTypes", DTOToBusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_DONATION_TYPE)));
		obj.put("paymentMethods", DTOToBusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_PAYMENT_METHODS)));
		obj.put("upiOptions", DTOToBusinessObjectConverter.toKeyValueList(domainRef.get(ITEM_UPI_OPTIONS)));
		if (donationType != null && currentStatus != null) {
			Optional<KeyValuePair> tarCurrStatus = kvStatus.stream()
					.filter(f -> f.getKey().equalsIgnoreCase(currentStatus.name())).findFirst();
			if (!tarCurrStatus.isEmpty()) {
				String[] nextStatusString = String
						.valueOf(tarCurrStatus.get().getAttributes().get(ATTR_NEXT_STATUS + donationType.name()))
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

	/**
	 * <----------------- DONATION HELPERS ENDS HERE ------------------------>
	 */

	/**
	 * <------------------ DONATION_ADDITIONAL_FIELDS HELPERS STARTS FROM
	 * HERE--------------------->
	 */
	private static final String ITEM_DONATION_ADDITIONAL_FIELDS = "DONATION_ADDITIONAL_FIELDS";
	private static final String ATTR_FIELD_TYPE = "TYPE";

	public FieldDTO checkAndConvertField(AdditionalField additionalField) throws Exception {
		List<KeyValuePair> kvFields = domainInfraService.getDomainRefConfigs().get(ITEM_DONATION_ADDITIONAL_FIELDS);
		KeyValuePair field = kvFields.stream().filter(f -> f.getKey().equalsIgnoreCase(additionalField.getKey()))
				.findFirst().orElseThrow(() -> new Exception("Invalid additional key"));
		FieldDTO fieldDTO=new FieldDTO();
		fieldDTO.setFieldId(additionalField.getId());
		fieldDTO.setFieldName(field.getValue());
		fieldDTO.setFieldType(String.valueOf(field.getAttributes().get(ATTR_FIELD_TYPE)));
		fieldDTO.setFieldKey(additionalField.getKey());
		fieldDTO.setFieldValue(additionalField.getValue());
		return fieldDTO;
	}
}
