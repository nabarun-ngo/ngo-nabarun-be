package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.domain.DonationDO;
import ngo.nabarun.app.businesslogic.domain.UserDO;
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.WorkFlowAction;
import ngo.nabarun.app.common.enums.WorkflowType;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkFlowDTO;

@Service
public class BaseBLImpl {
	
	@Autowired
	protected BusinessHelper businessHelper;

	@Autowired
	protected GenericPropertyHelper propertyHelper;
	
	@Autowired
	protected UserDO userDO;
	
	@Autowired
	protected DonationDO donationDO;
	
	protected WorkFlowDTO performWorkflowAction(WorkFlowAction action, WorkFlowDTO workflow) throws Exception {
		switch (action) {
		case ONBOARD_USER:
			boolean emailVerified = workflow.getWorkflowType() == WorkflowType.JOIN_REQUEST;
			UserDTO memeber = onboardMember(workflow.getAdditionalFields(), emailVerified);
			workflow.setRequester(memeber);
			break;
		case ENTRY_GUEST_DONATION:
			createGuestDonation(workflow.getAdditionalFields());
			break;
		case PAYMENY_NOT_FOUND:

			break;
		case NO_ACTION:
			break;
		}
		return workflow;
	}

	private void createGuestDonation(List<FieldDTO> additionalFields) {
		// TODO Auto-generated method stub

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
		UserDetail user = userDO.createUser(firstName, lastName, email, phoneCode, phoneNumber, hometown, password, emailVerified);
		return BusinessObjectConverter.toUserDTO(user);
	}
}
