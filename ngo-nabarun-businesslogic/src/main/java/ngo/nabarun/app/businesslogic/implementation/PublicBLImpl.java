package ngo.nabarun.app.businesslogic.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;

import ngo.nabarun.app.businesslogic.IPublicBL;
import ngo.nabarun.app.businesslogic.businessobjects.InterviewDetail;
import ngo.nabarun.app.businesslogic.businessobjects.InterviewDetail.UserAction;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentMapping;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary.PayableAccDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UPIDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.domain.AccountDO;
import ngo.nabarun.app.businesslogic.domain.RequestDO;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountType;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.PublicPage;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.RequestDTO;

@Service
public class PublicBLImpl extends BaseBLImpl implements IPublicBL {

	@Autowired
	private AccountDO accountDO;

	@Autowired
	private RequestDO requestDO;

	@Autowired
	private PropertyHelper prop;

	private static List<UserDetail> users;

	@Override
	public Map<String, Object> getPageData(PublicPage pageName) throws Exception {
		Map<String, Object> pageDataMap = new HashMap<>();

		pageDataMap.put("VERSION", CommonUtils.getAppVersion());
		pageDataMap.put("content", getPageContent(prop));

		switch (pageName) {
		case HOME:
			pageDataMap.put("interview", new InterviewDetail());
			pageDataMap.put("profiles", getTeamProfiles());
			pageDataMap.put("phoneCodes", businessHelper.getPhoneCodes());
			break;
		case POLICY:
		case JOIN:
			businessHelper.getPolicyDocs()
			.forEach(kv -> pageDataMap.put(kv.getKey(), kv.getValue()));
			break;
		default:
			break;
		}
		return pageDataMap;

	}

	private List<UserDetail> getTeamProfiles() throws Exception {
		if (users == null) {
			UserDetailFilter filter = new UserDetailFilter();
			filter.setPublicFlag(true);
			users = userDO.retrieveAllUsers(null, null, filter)
					.map(m -> BusinessObjectConverter.toPublicUserDetail(m, null)).getContent();
		}
		return users;
	}

	private Map<String, Object> getPageContent(PropertyHelper prop) throws Exception {
		String content = businessHelper.getDomainJsonContent(true);
		Map<String, String> replacements = new HashMap<>();
		replacements.put("##LOGIN_URL##", prop.getAppLoginURL());

		for (var entry : replacements.entrySet()) {
			content = content.replace(entry.getKey(), entry.getValue());
		}
		return CommonUtils.getObjectMapper().readValue(content, new TypeReference<Map<String, Object>>() {
		});
	}

	@Override
	public InterviewDetail signUp(InterviewDetail interview) throws Exception {

		if (interview.getBreadCrumb().size() == 0) {
			interview.getBreadCrumb().add("Home");
		}
		if (interview.getActionName() == UserAction.SUBMIT_BASIC_INFO) {
			interview.setStage("1");
			interview.getBreadCrumb().add("Rules and Regulations");
		} else if (interview.getActionName() == UserAction.ACCEPT_RULES) {
			
			interview.setStage("1A");
			interview.getBreadCrumb().add("Additional Details");
		} else if (interview.getActionName() == UserAction.SUBMIT_ADDITIONAL_DETAIL) {
			String description = businessHelper.getPasswordPolicyDescription(userDO.getPasswordPolicy());
			interview.setMessage(description);
			interview.setStage("2");
			interview.getBreadCrumb().add("Login Details");
		} else if (interview.getActionName() == UserAction.SUBMIT_LOGIN_DETAIL) {
			String name = interview.getFirstName();
			String email = interview.getEmail();
			String mobileNo = interview.getDialCode() + interview.getContactNumber();
			String password = interview.getPassword();

			businessHelper.throwBusinessExceptionIf(() -> userDO.isUserExists(email),
					ExceptionEvent.EMAIL_ALREADY_IN_USE);

			String passwordRegex = businessHelper.getPasswordPolicyRegex(userDO.getPasswordPolicy());
			businessHelper.throwBusinessExceptionIf(() -> PasswordUtils.isPasswordValid(password, passwordRegex),
					ExceptionEvent.PASSWORD_NOT_COMPLIANT);

			String token = requestDO.sendOTP(name, email, mobileNo, "Sign up", null);
			interview.setMessage("One Time Password has been sent to <b id='id'>" + email + "</b>");
			interview.setOtpToken(token);
			interview.setSiteKey(propertyHelper.getGoogleRecaptchaSiteKey());
			interview.setStage("3");
			interview.getBreadCrumb().add("Verify and Submit");
		} else if (interview.getActionName() == UserAction.RESEND_OTP) {
			requestDO.reSendOTP(interview.getOtpToken());
			interview.setMessage("One Time Password has been sent to " + interview.getEmail());
			interview.setStage("3");
			interview.getBreadCrumb().add("Verify and Submit");
		} else if (interview.getActionName() == UserAction.SUBMIT_OTP) {
			requestDO.validateOTP(interview.getOtpToken(), interview.getOnetimePassword(), "Sign up");
			List<AdditionalField> addFieldList = new ArrayList<>();
			addFieldList.add(new AdditionalField(AdditionalFieldKey.firstName, interview.getFirstName()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.lastName, interview.getLastName()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.email, interview.getEmail()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.dialCode, interview.getDialCode()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.mobileNumber, interview.getContactNumber()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.hometown, interview.getHometown()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.reasonForJoining, interview.getReasonForJoining()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.howDoUKnowAboutNabarun,
					interview.getHowDoUKnowAboutNabarun()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.password, interview.getPassword()));

			RequestDetail request = new RequestDetail();
			request.setDescription(
					"I want to join NABARUN for '" + interview.getReasonForJoining() + "'. Please do needful.");
			request.setAdditionalFields(addFieldList);
			request.setType(RequestType.JOIN_REQUEST);
			request.setDelegated(false);

			UserDetail requester = new UserDetail();
			requester.setFirstName(interview.getFirstName());
			requester.setLastName(interview.getLastName());
			requester.setEmail(interview.getEmail());
			request.setRequester(requester);

			RequestDTO requestDTO = requestDO.createRequest(request, true, null, (t, u) -> {
				return performWorkflowAction(t, u);
			});

			interview.setStage("POST_SUBMIT");
			interview.getBreadCrumb().add("Request Submitted");
			interview.setMessage("Thank you for your interest. Your request number is <b id='id'>" + requestDTO.getId()
					+ "</b>. We will connect you very shortly.");
		}

		return interview;
	}

	@Override
	public InterviewDetail initDonation(InterviewDetail interview) throws Exception {
		if (interview.getBreadCrumb().size() == 0) {
			interview.getBreadCrumb().add("Home");
		}
		if (interview.getActionName() == UserAction.SUBMIT_PAYMENT_INFO) {
			List<PayableAccDetail> accounts = accountDO.retrievePayableAccounts(AccountType.PUBLIC_DONATION).stream()
					.map(m -> {
						PayableAccDetail pad = new PayableAccDetail();
						pad.setId(m.getId());
						pad.setPayableBankDetails(BusinessObjectConverter.toBankDetail(m.getBankDetail()));
						UPIDetail upiDetail = BusinessObjectConverter.toUPIDetail(m.getUpiDetail());
						upiDetail.setQrData(CommonUtils.getUPIURI(upiDetail.getUpiId(), upiDetail.getPayeeName(),
								interview.getAmount(), null, null, null));
						pad.setPayableUPIDetail(upiDetail);
						return pad;
					}).collect(Collectors.toList());
			interview.setAccounts(accounts);
			interview.setStage("MAKE_PAYMENT");
			interview.getBreadCrumb().add("Make Payment");
		} else if (interview.getActionName() == UserAction.CONFIRM_PAYMENT) {
			List<AdditionalField> addFieldList = new ArrayList<>();
			addFieldList.add(new AdditionalField(AdditionalFieldKey.name, interview.getFirstName()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.email, interview.getEmail()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.dialCode, interview.getDialCode()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.mobileNumber, interview.getContactNumber()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.amount, String.valueOf(interview.getAmount())));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.paymentMethod, interview.getPaymentMethod()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.paidToAccount, interview.getPaidToAccountId()));

			RequestDetail request = new RequestDetail();
			request.setDescription("Please check and confirm payment.");
			request.setAdditionalFields(addFieldList);
			request.setType(RequestType.CHECK_PAYMENT_GUEST);
			request.setDelegated(false);

			UserDetail requester = new UserDetail();
			requester.setFirstName(interview.getFirstName());
			requester.setLastName("");
			requester.setEmail(interview.getEmail());
			request.setRequester(requester);

			RequestDTO requestDTO = requestDO.createRequest(request, true, null, (t, u) -> {
				return performWorkflowAction(t, u);
			});

			/*
			 * 
			 */
			if (interview.getFiles() != null && interview.getFiles().length > 0) {
				for (MultipartFile file : interview.getFiles()) {
					DocumentMapping documentMapping = new DocumentMapping();
					documentMapping.setDocIndexId(requestDTO.getId());
					documentMapping.setDocIndexType(DocumentIndexType.REQUEST);
					requestDO.uploadDocument(file, List.of(documentMapping));
				}
			}

			interview.setStage("POST_SUBMIT");
			interview.getBreadCrumb().add("Payment Completed");
			interview.setMessage("Your request number is <b id='id'>" + requestDTO.getId()
					+ "</b>. We will check and confirm about this payment soonest.");

		} else if (interview.getActionName() == UserAction.SUBMIT_REQUEST) {
			List<AdditionalField> addFieldList = new ArrayList<>();
			addFieldList.add(new AdditionalField(AdditionalFieldKey.name, interview.getFirstName()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.email, interview.getEmail()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.dialCode, interview.getDialCode()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.mobileNumber, interview.getContactNumber()));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.amount, String.valueOf(interview.getAmount())));
			addFieldList.add(new AdditionalField(AdditionalFieldKey.paymentMethod, interview.getPaymentMethod()));

			RequestDetail request = new RequestDetail();
			request.setDescription("Please collect cash payment.");
			request.setAdditionalFields(addFieldList);
			request.setType(RequestType.COLLECT_CASH_PAYMENT);
			request.setDelegated(false);

			UserDetail requester = new UserDetail();
			requester.setFirstName(interview.getFirstName());
			requester.setLastName("");
			requester.setEmail(interview.getEmail());
			request.setRequester(requester);

			RequestDTO requestDTO = requestDO.createRequest(request, true, null, (t, u) -> {
				return performWorkflowAction(t, u);
			});

			interview.setStage("POST_SUBMIT");
			interview.getBreadCrumb().add("Request Submitted");
			interview.setMessage("Your request number is <b id='id'>" + requestDTO.getId()
					+ "</b>. We will connect very shortly to collect your cash payment.");
		}
		return interview;
	}

	@Override
	public InterviewDetail contact(InterviewDetail interview) throws Exception {
		if (interview.getBreadCrumb().size() == 0) {
			interview.getBreadCrumb().add("Home");
		}
		List<CorrespondentDTO> recipients = new ArrayList<>();

		recipients
				.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.CC).name(interview.getFirstName())
						.email(interview.getEmail()).mobile(interview.getContactNumber()).build());
		List<UserDTO> users = userDO.getUsers(
				List.of(RoleCode.PRESIDENT, RoleCode.VICE_PRESIDENT, RoleCode.SECRETARY, RoleCode.ASST_SECRETARY,
						RoleCode.GROUP_COORDINATOR, RoleCode.ASST_GROUP_COORDINATOR, RoleCode.TECHNICAL_SPECIALIST));
		for (UserDTO user : users) {
			recipients.add(CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO).name(user.getName())
					.email(user.getEmail()).mobile(user.getPhoneNumber()).build());
		}
		/**
		 * Sending notification and email
		 */
		requestDO.sendEmailAsync(BusinessConstants.EMAILTEMPLATE__PUBLIC_QUERY, recipients,
				Map.of("interview", interview), interview.getEmail(), interview.getFirstName());
		requestDO.sendNotification(BusinessConstants.NOTIFICATION__PUBLIC_QUERY, Map.of(), users, "New Query");

		interview.setStage("POST_SUBMIT");
		interview.getBreadCrumb().add("Request Submitted");
		interview.setMessage("We have acknowledged your request. We will connect you shortly.");
		return interview;
	}
}
