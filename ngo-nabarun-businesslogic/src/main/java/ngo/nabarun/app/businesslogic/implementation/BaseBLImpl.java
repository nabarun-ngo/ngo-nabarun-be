package ngo.nabarun.app.businesslogic.implementation;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.domain.CommonDO;
import ngo.nabarun.app.businesslogic.domain.DonationDO;
import ngo.nabarun.app.businesslogic.domain.UserDO;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.WorkAction;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.RequestDTO;

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
	
	protected RequestDTO performWorkflowAction(WorkAction action, RequestDTO workflow) throws Exception {
		switch (action) {
		case ONBOARD_USER:
			boolean emailVerified = workflow.getType() == RequestType.JOIN_REQUEST;
			UserDTO memeber = onboardMember(workflow.getAdditionalFields(), emailVerified);
			workflow.setRequester(memeber);
			workflow.setLastActionCompleted(true);
			break;
		case ENTRY_GUEST_DONATION:
			createGuestDonation(workflow.getAdditionalFields(),workflow.getCreatedOn());
			workflow.setLastActionCompleted(true);
			break;
		case PAYMENY_NOT_FOUND:
			workflow.setLastActionCompleted(true);
			break;
		case NO_ACTION:
			workflow.setLastActionCompleted(true);
			break;
		}
		return workflow;
	}

	private void createGuestDonation(List<FieldDTO> fields,Date payDate) throws Exception {
		DonationDetail donation=new DonationDetail();
		donation.setDonationType(DonationType.ONETIME);
		donation.setIsGuest(true);
		UserDetail donor=new UserDetail();
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
		DonationDTO newDon=donationDO.raiseDonation(donation);
		donation= new DonationDetail();
		donation.setDonationStatus(DonationStatus.PAID);
		donation.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
		AccountDetail paidTo= new AccountDetail();
		paidTo.setId(paidToAccount);
		donation.setReceivedAccount(paidTo);
		donation.setPaidOn(payDate);
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		donationDO.updateDonation(newDon.getId(), donation, userId);
	}

	private UserDTO onboardMember(List<FieldDTO> fields, boolean emailVerified) throws Exception {
		String firstName = null;
		String lastName= null;
		String email= null;
		String phoneCode= null;
		String phoneNumber= null;
		String hometown= null;
		String password= null;
		for (FieldDTO field : fields) {
			switch (field.getFieldKey()) {
			case firstName:
				firstName = field.getFieldValue();
				break;
			case lastName:
				lastName=field.getFieldValue();
				break;
			case email:
				email=field.getFieldValue();
				break;
			case dialCode:
				phoneCode=field.getFieldValue();
				break;
			case mobileNumber:
				phoneNumber=field.getFieldValue();
				break;
			case hometown:
				hometown=field.getFieldValue();
				break;
			case password:
				password=field.getFieldValue();
				break;
			default:
				break;
			}
		}
		UserDTO user = userDO.createUser(firstName, lastName, email, phoneCode, phoneNumber, hometown, password, emailVerified);
		return user;
	}
}
