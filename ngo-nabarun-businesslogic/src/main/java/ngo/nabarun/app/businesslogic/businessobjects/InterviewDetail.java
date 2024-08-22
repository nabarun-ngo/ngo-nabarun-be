package ngo.nabarun.app.businesslogic.businessobjects;


import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary.PayableAccDetail;

@Data
public class InterviewDetail {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("actionName")
	private UserAction actionName;
	
	@JsonProperty("firstName")
	private String firstName;

	@JsonProperty("middleName")
	private String middleName;

	@JsonProperty("lastName")
	private String lastName;

	@JsonProperty("email")
	private String email;

	@JsonProperty("dialCode")
	private String dialCode;

	@JsonProperty("mobileNumber")
	private String contactNumber;

	@JsonProperty("hometown")
	private String hometown;

	@JsonProperty("reasonForJoining")
	private String reasonForJoining;
	
	@JsonProperty("howDoUKnowAboutNabarun")
	private String howDoUKnowAboutNabarun;

	@JsonProperty("attachedToAnyOtherSocialWork")
	private String anyOtherSocialWork;
	
	@JsonProperty("files")
	private MultipartFile[] files;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("confirmPassword")
	private String confirmPassword;

	@JsonProperty("onetimePassword")
	private String onetimePassword;
	
	@JsonProperty("siteKey")
	private String siteKey;
	
	@JsonProperty("paymentMethod")
	private String paymentMethod;
	
	@JsonProperty("paidToAccountId")
	private String paidToAccountId;
	
	@JsonProperty("amount")
	private Double amount;
	
	
	@JsonProperty("subject")
	private String subject;
	
	@JsonIgnore
	private String stage;
	
	@JsonIgnore
	private List<String> breadCrumb=new ArrayList<>();
	
	@JsonIgnore
	private List<PayableAccDetail> accounts=new ArrayList<>();
	
	@JsonIgnore
	private String message;
	
	@JsonIgnore
	private List<String> rules;
	
	@JsonProperty("otpToken")
	private String otpToken;
	
	public enum UserAction{
		SUBMIT_BASIC_INFO,ACCEPT_RULES,SUBMIT_LOGIN_DETAIL,RESEND_OTP,SUBMIT_OTP, SUBMIT_PAYMENT_INFO, CONFIRM_PAYMENT, SUBMIT_REQUEST
	}
}
