package ngo.nabarun.app.businesslogic.implementation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.domain.CommonDO;
import ngo.nabarun.app.businesslogic.domain.DonationDO;
import ngo.nabarun.app.businesslogic.domain.RequestDO;
import ngo.nabarun.app.businesslogic.domain.UserDO;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.WorkAction;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;
import ngo.nabarun.app.infra.dto.WorkDTO.WorkDTOFilter;
import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;

@Service
public class BaseBLImpl {

	@Autowired
	protected BusinessDomainHelper businessHelper;

	@Autowired
	protected PropertyHelper propertyHelper;

	@Autowired
	protected UserDO userDO;

	@Autowired
	protected DonationDO donationDO;

	@Autowired
	protected CommonDO commonDO;
	
	@Autowired
	protected RequestDO requestDO;
	
	protected RequestDTO performWorkflowAction(WorkAction action, RequestDTO workflow) throws Exception {
		UserDTO memeber;
		boolean emailVerified;
		boolean resetPassword;
		switch (action) {
		case ONBOARD_USER:
			emailVerified = workflow.getType() == RequestType.JOIN_REQUEST;
			resetPassword = workflow.getType() == RequestType.JOIN_REQUEST_USER;
			memeber = onboardMember(workflow.getAdditionalFields(), emailVerified, resetPassword);
			sendOnboardingEmail(workflow, resetPassword);
			workflow.setRequester(memeber);
			workflow.setLastActionCompleted(true);
			break;
		case ENTRY_GUEST_DONATION:
			createGuestDonation(workflow);
			workflow.setLastActionCompleted(true);
			break;
		case PAYMENY_NOT_FOUND:
			workflow.setLastActionCompleted(true);
			break;
		case NO_ACTION:
			workflow.setLastActionCompleted(true);
			break;
		case EXIT_USER:
			exitUser(workflow);
			workflow.setLastActionCompleted(true);
			break;
		case PROCESS_DONATION_PAUSE:
			pauseDonation(workflow);
			workflow.setLastActionCompleted(true);
			break;
		}
		return workflow;
	}

	private void exitUser(RequestDTO workflow) throws Exception {
		String id=workflow.getRequester().getProfileId();
		donationDO.convertMemberToGuestAndCloseAccount(id);
		userDO.deleteMember(id);
	}

	private void pauseDonation(RequestDTO workflow) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = null;
		Date endDate = null;
		for (FieldDTO field : workflow.getAdditionalFields()) {
			switch (field.getFieldKey()) {
			case startDate:
				startDate = dateFormat.parse(field.getFieldValue());
				break;
			case endDate:
				endDate = dateFormat.parse(field.getFieldValue());
				break;
			default:
				break;
			}
		}
		UserDTO userDTO= new UserDTO();
		UserAdditionalDetailsDTO uadDTO = new UserAdditionalDetailsDTO();
		uadDTO.setDonPauseStartDate(startDate);
		uadDTO.setDonPauseEndDate(endDate);
		userDTO.setAdditionalDetails(uadDTO);
		userDO.updateUserDetailAdmin(workflow.getRequester().getProfileId(), userDTO);
	}

	private void sendOnboardingEmail(RequestDTO workflow, boolean sendPassword) throws Exception {
		String firstName = null;
		String email = null;
		String password = null;
		List<FieldDTO> fields=workflow.getAdditionalFields();
		for (FieldDTO field : fields) {
			switch (field.getFieldKey()) {
			case firstName:
				firstName = field.getFieldValue();
				break;
			case email:
				email = field.getFieldValue();
				break;
			case password:
				password = field.getFieldValue();
				break;
			default:
				break;
			}
		}
		Optional<KeyValue> loginURL = businessHelper.getNabarunOrgInfo().stream()
				.filter(f -> f.getKey().equalsIgnoreCase("LOGIN_URL")).findFirst();
		List<CorrespondentDTO> corrDTO = new ArrayList<>();
		corrDTO.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO).email(email).name(firstName)
				.build());
		Map<String, String> user = new HashMap<>();
		user.put("name", firstName);
		user.put("email", email);
		user.put("password", sendPassword ? password : "[Password entered in online form]");
		if (!loginURL.isEmpty()) {
			user.put("portalLink", loginURL.get().getValue());
		}
		userDO.sendEmailAsync(BusinessConstants.EMAILTEMPLATE__ON_USER_ONBOARDING, corrDTO, Map.of("user", user),workflow.getId(),null);
	}

	private void createGuestDonation(RequestDTO workflow) throws Exception {
		List<FieldDTO> fields=workflow.getAdditionalFields();
		Date payDate=workflow.getCreatedOn();
		DonationDetail donation = new DonationDetail();
		donation.setDonationType(DonationType.ONETIME);
		donation.setIsGuest(true);
		UserDetail donor = new UserDetail();
		String paymentMethod = null;
		String paidToAccount = null;
		for (FieldDTO field : fields) {
			switch (field.getFieldKey()) {
			case name:
				donor.setFullName(field.getFieldValue());
				break;
			case email:
				donor.setEmail(field.getFieldValue());
				break;
			case mobileNumber:
				donor.setPrimaryNumber(field.getFieldValue());
				break;
			case amount:
				donation.setAmount(Double.valueOf(field.getFieldValue()));
				break;
			case paymentMethod:
				paymentMethod = field.getFieldValue();
				break;
			case paidToAccount:
				paidToAccount = field.getFieldValue();
				break;
			default:
				break;
			}
		}
		donation.setDonorDetails(donor);
		DonationDTO newDon = donationDO.raiseDonation(donation);
		donation = new DonationDetail();
		donation.setDonationStatus(DonationStatus.PAID);
		donation.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
		AccountDetail paidTo = new AccountDetail();
		paidTo.setId(paidToAccount);
		donation.setReceivedAccount(paidTo);
		donation.setPaidOn(payDate);
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		donationDO.updateDonation(newDon.getId(), donation, userId);
		donationDO.cloneDocuments(workflow.getId(), DocumentIndexType.REQUEST,newDon.getId(),DocumentIndexType.DONATION);
	} 

	private UserDTO onboardMember(List<FieldDTO> fields, boolean emailVerified, boolean resetPassword)
			throws Exception {
		String firstName = null;
		String lastName = null;
		String email = null;
		String phoneCode = null;
		String phoneNumber = null;
		String hometown = null;
		String password = null;
		for (FieldDTO field : fields) {
			switch (field.getFieldKey()) {
			case firstName:
				firstName = field.getFieldValue();
				break;
			case lastName:
				lastName = field.getFieldValue();
				break;
			case email:
				email = field.getFieldValue();
				break;
			case dialCode:
				phoneCode = field.getFieldValue();
				break;
			case mobileNumber:
				phoneNumber = field.getFieldValue();
				break;
			case hometown:
				hometown = field.getFieldValue();
				break;
			case password:
				password = field.getFieldValue();
				break;
			default:
				break;
			}
		}
		UserDTO user = userDO.createUser(firstName, lastName, email, phoneCode, phoneNumber, hometown, password,
				emailVerified, resetPassword);
		/**
		 * Creating request for mandatory details update
		 */
		RequestDTO request = new RequestDTO();
		request.setType(RequestType.PROFILE_UPDATE_REQUEST);
		request.setSystemRequestOwner(user);
		request.setSystemGenerated(true);
		request.setRefId(user.getProfileId());
		requestDO.createRequest(request, false, null, (t, u) -> {
			return performWorkflowAction(t, u);
		});
		return user;
	}
	
	protected void closeLinkedWorkItem(String linkId,RequestType sourceType, List<AdditionalField> addnlField) throws Exception {
		String userId = SecurityUtils.getAuthUserId();
		WorkDTOFilter filter= new WorkDTOFilter();
		filter.setSourceRefId(linkId);
		filter.setStepCompleted(false);
		filter.setSourceType(sourceType);
		List<WorkDTO> workItems=requestDO.retrieveAllWorkItems(null, null, filter).getContent();
		for(WorkDTO workItem:workItems) {
			WorkDetail workDetail=new WorkDetail();
			workDetail.setAdditionalFields(addnlField);
			requestDO.updateWorkItem(workItem, workDetail, userId, ((t, u) -> performWorkflowAction(t, u)));
		}
	}
}
