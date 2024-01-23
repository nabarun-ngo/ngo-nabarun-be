package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import ngo.nabarun.app.common.enums.ProfileStatus;

@Getter
@Setter
public class UserDetail {
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("initials")
	private String initials;
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("firstName")
	private String firstName;
	
	@JsonProperty("middleName")
	private String middleName;
	
	@JsonProperty("lastName")
	private String lastName;
	
	@JsonProperty("fullName")
	private String fullName;
	
	@JsonProperty("dateOfBirth")
	private Date dateOfBirth;
	
	@JsonProperty("gender")
	private String gender;
	
	@JsonProperty("about")
	private String about;
	
	@JsonProperty("picture")
	private String picture;
	
	@JsonProperty("roles")
	private List<UserRole> roles;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("phones")
	private List<UserPhoneNumber> phoneNumbers;
	
	@JsonProperty("addresses")
	private List<UserAddress> addresses;
	
	@JsonProperty("socialMediaLinks")
	private List<UserSocialMedia> socialMediaLinks;
	
	@JsonProperty("memberSince")
	private String memberSince;
	
	@JsonProperty("activeContributor")
	private String activeContributor;
	
	@JsonProperty("publicProfile")
	private boolean publicProfile;
	
	@JsonProperty("userId")
	private String userId;
	
	@JsonProperty("status")
	private ProfileStatus status;
	
}
