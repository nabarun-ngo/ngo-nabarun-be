package ngo.nabarun.app.businesslogic.businessobjects;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class JoiningInterviewDetail {

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

}
