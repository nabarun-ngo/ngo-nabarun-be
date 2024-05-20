package ngo.nabarun.app.businesslogic.businessobjects;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SignUpDetail {
	
	@JsonProperty("actionName")
	private JoinAction actionName;
	
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
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("confirmPassword")
	private String confirmPassword;

	@JsonProperty("onetimePassword")
	private String onetimePassword;
	
	@JsonProperty("siteKey")
	private String siteKey;
	
	@JsonIgnore
	private int stageId;
	
	@JsonIgnore
	private List<String> breadCrumb=new ArrayList<>();
	
	@JsonIgnore
	private String message;
	
	@JsonIgnore
	private List<String> rules;
	
	@JsonProperty("otpToken")
	private String otpToken;
	
	public enum JoinAction{
		SUBMIT_BASIC_INFO,ACCEPT_RULES,SUBMIT_LOGIN_DETAIL,RESEND_OTP,SUBMIT_OTP
	}
}
