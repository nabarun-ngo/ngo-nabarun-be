package ngo.nabarun.app.businesslogic.implementation;

import java.util.ArrayList;
import java.util.List;

import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.IPublicBL;
import ngo.nabarun.app.businesslogic.businessobjects.SignUpDetail;
import ngo.nabarun.app.businesslogic.businessobjects.SignUpDetail.JoinAction;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.WorkflowType;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.UserDTO.UserDTOFilter;
import ngo.nabarun.app.infra.dto.WorkFlowDTO;
import ngo.nabarun.app.infra.dto.WorkListDTO;
import ngo.nabarun.app.infra.service.IUserInfraService;
import ngo.nabarun.app.infra.service.IWorkflowInfraService;

@Service 
public class PublicBLImpl implements IPublicBL{
	
	@Autowired
	private IUserInfraService userInfraService;
	
	@Autowired
	private BusinessHelper businessHelper;
	
	@Autowired
	private GenericPropertyHelper genericPropertyHelper;
	
	private static String passwordPolicy;

	@Autowired
	private IWorkflowInfraService workflowInfraService;
	
	
	@Override
	public SignUpDetail signUp(SignUpDetail interview) throws Exception {
		
		if(interview.getBreadCrumb().size() == 0) {
			interview.getBreadCrumb().add("Home");
		}
		if(interview.getActionName() == JoinAction.SUBMIT_BASIC_INFO) {
			List<String> rules=businessHelper.getRules().stream().map(m->m.getValue()).toList();
			interview.setRules(rules);
			interview.setStageId(1);
			interview.getBreadCrumb().add("Rules and Regulations");
		}
		else if(interview.getActionName() == JoinAction.ACCEPT_RULES) {
			/*
			 * Fetching password policy description
			 */
			try {
				if(passwordPolicy == null) {
					passwordPolicy=userInfraService.getPaswordPolicy();
				}
				String description= businessHelper.getPasswordPolicyDescription(passwordPolicy);
				interview.setMessage(description);
			}catch (Exception e) {
				e.printStackTrace();
			}
			interview.setStageId(2);
			interview.getBreadCrumb().add("Login Details");
		}
		else if(interview.getActionName() == JoinAction.SUBMIT_LOGIN_DETAIL) {
			UserDTOFilter filter = new UserDTOFilter();
			filter.setEmail(interview.getEmail());
			int userCount=userInfraService.getUsers(null, null, filter).getSize();
			businessHelper.throwBusinessExceptionIf(()->userCount > 0, ExceptionEvent.EMAIL_ALREADY_IN_USE);
			if(passwordPolicy == null) {
				passwordPolicy=userInfraService.getPaswordPolicy();
			}		
			RuleResult result=PasswordUtils.validatePassword(interview.getPassword(), businessHelper.getPasswordPolicyRegex(passwordPolicy));
			businessHelper.throwBusinessExceptionIf(()->result.isValid(), ExceptionEvent.PASSWORD_NOT_COMPLIANT);
			/**
			 * Sending OTP to client
			 */
			String mobileNo=interview.getDialCode()+interview.getContactNumber();
			String token=businessHelper.sendOTP(interview.getFirstName(), interview.getEmail(),mobileNo, "Sign up", null);
			interview.setMessage("One Time Password has been sent to "+interview.getEmail());
			interview.setOtpToken(token);
			interview.setSiteKey(genericPropertyHelper.getGoogleRecaptchaSiteKey());
			interview.setStageId(3);
			interview.getBreadCrumb().add("Verify and Submit");
		}
		else if(interview.getActionName() == JoinAction.RESEND_OTP) {
			businessHelper.reSendOTP(interview.getOtpToken());
			interview.setMessage("One Time Password has been sent to "+interview.getEmail());
			interview.setStageId(3);
			interview.getBreadCrumb().add("Verify and Submit");
		}else if(interview.getActionName() == JoinAction.SUBMIT_OTP) {
			businessHelper.validateOTP(interview.getOtpToken(),interview.getOnetimePassword(),"Sign up");
			List<AdditionalField> addFieldList= new ArrayList<>();
			addFieldList.add(new AdditionalField(AdditionalFieldKey.firstName, interview.getFirstName(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.lastName, interview.getLastName(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.email, interview.getEmail(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.dialCode, interview.getDialCode(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.mobileNumber, interview.getContactNumber(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.hometown, interview.getHometown(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.reasonForJoining, interview.getReasonForJoining(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.howDoUKnowAboutNabarun, interview.getHowDoUKnowAboutNabarun(), true));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.password, interview.getPassword(), false,true));
			WorkFlowDTO workflow= businessHelper.convertToWorkflowDTO(WorkflowType.JOIN_REQUEST, addFieldList);
			UserDTO requester= new UserDTO();
			requester.setFirstName(interview.getFirstName());
			requester.setLastName(interview.getLastName());
			requester.setEmail(interview.getEmail());
			workflow.setRequester(requester);
			workflow.setDelegated(false);
			WorkListDTO worklist=businessHelper.prepareWorkList(workflow.getWorkflowType(),workflow.getWorkflowStatus(),null);
			workflow.setWorkflowDescription("I want to join NABARUN for "+interview.getReasonForJoining()+". Please do needful.");
			workflow.setId(businessHelper.generateWorkflowId());
			workflow = workflowInfraService.createWorkflow(workflow);
			worklist.setWorkflowId(workflow.getId());
			worklist.setPendingWithUsers(userInfraService.getUsersByRole(worklist.getPendingWithRoles()));
			workflowInfraService.createWorkList(worklist);
			
			interview.setStageId(4);
			interview.getBreadCrumb().add("Request Submitted");
			interview.setMessage("Thank you for your interest. Your request number is "+workflow.getId()+". We will connect you very shortly.");

		}
		return interview;
	}


	@Override
	public List<KeyValue> getOrganizationInfo() throws Exception {
		return businessHelper.getNabarunOrgInfo();
	}
}
