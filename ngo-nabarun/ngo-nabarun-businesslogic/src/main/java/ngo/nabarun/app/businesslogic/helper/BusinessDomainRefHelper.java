package ngo.nabarun.app.businesslogic.helper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;
import ngo.nabarun.app.infra.service.IGlobalDataInfraService;

@Service
public class BusinessDomainRefHelper {
	
	@Autowired 
	private IGlobalDataInfraService domainInfraService;

	private static final String SPLITTER= ",";
	private static final String STAR= "*";
	

	/**
	 * ---------------------------------------
	 * USER HELPERS STARTS FROM HERE
	 * ---------------------------------------
	 */
	private static final String ITEM_USER_TITLE = "USER_TITLES";
	private static final String ITEM_USER_GENDER = "USER_GENDERS";

	private static final String ATTR_GENDER = "GENDER";


	public boolean isTitleGenderAligned(String title, String gender)
			throws Exception {
		List<KeyValuePair> kvTitle = domainInfraService.getDomainRefConfigs().get(ITEM_USER_TITLE);

		Optional<KeyValuePair> userTitle = kvTitle.stream()
				.filter(f -> f.getKey().equalsIgnoreCase(title)).findFirst();
		if (userTitle.isEmpty()) {
			return false;
		}
		String allowedGender = String.valueOf(userTitle.get().getAttributes().get(ATTR_GENDER));
		return allowedGender.contains(STAR) || allowedGender.toUpperCase().contains(gender.toUpperCase());
	}

	public String getGenderValue(String key) throws Exception {
		List<KeyValuePair> kvGender = domainInfraService.getDomainRefConfigs().get(ITEM_USER_GENDER);
		Optional<KeyValuePair> gender = kvGender.stream()
				.filter(f -> f.getKey().equalsIgnoreCase(key)).findFirst();
		return gender.isEmpty() ? key : gender.get().getValue();
	}

	public String getTitleValue(String titleKey) throws Exception {
		Optional<KeyValuePair> title = domainInfraService.getDomainRefConfigs().get(ITEM_USER_TITLE).stream()
				.filter(f -> f.getKey().equalsIgnoreCase(titleKey)).findFirst();
		return title.isEmpty() ? titleKey : title.get().getValue();
	}
	
	/**
	 * -----------------------------------------
	 * USER HELPERS ENDS HERE
	 * -----------------------------------------
	 */
	
	/**
	 * ---------------------------------------
	 * ROLES HELPERS STARTS FROM HERE
	 * ---------------------------------------
	 */
//	public static String isSameRoleGroup(UserConfigTemplate userConfig,String roleCode) throws Exception {
//	Optional<KeyValuePair> title=userConfig.getUserTitles().stream().filter(f->f.getKey().equalsIgnoreCase(titleKey)).findFirst();
//	return title.isEmpty() ? titleKey : title.get().getDisplayValue();
//}
//
	
	/**
	 * -----------------------------------------
	 * ROLES HELPERS ENDS HERE
	 * -----------------------------------------
	 */


	/**
	 * ---------------------------------------
	 * DONATION HELPERS STARTS FROM HERE
	 * ---------------------------------------
	 */
	private static final String ITEM_DONATION_STATUS = "DONATION_STATUSES";
	private static final String ATTR_IS_FINAL_STATUS = "IS_FINAL_STATUS";
	private static final String ATTR_NEXT_STATUS = "NEXT_STATUS-";


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
	
	public List<DonationStatus> getNextDonationStatus(DonationType donationType,DonationStatus currentStatus) throws Exception {
		List<KeyValuePair> kvStatus = domainInfraService.getDomainRefConfigs().get(ITEM_DONATION_STATUS);
		KeyValuePair tarCurrStatus = kvStatus.stream().filter(f->f.getKey() == currentStatus.name()).findFirst().get();
		String[] nextStatusString=String.valueOf(tarCurrStatus.getAttributes().get(ATTR_NEXT_STATUS+donationType.name())).split(SPLITTER);	
		return List.of(nextStatusString).stream().map(m->DonationStatus.valueOf(m)).collect(Collectors.toList());
	}
	/**
	 * -----------------------------------------
	 * DONATION HELPERS ENDS HERE
	 * -----------------------------------------
	 */



}
