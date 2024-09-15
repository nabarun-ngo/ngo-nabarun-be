package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.AddressType;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.SocialMediaType;

@Data
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
	
	@JsonProperty("pictureBase64")
	private String pictureBase64;
	
	@JsonProperty("roles")
	private List<UserRole> roles;
	
	@JsonProperty("roleString")
	private String roleString;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("primaryNumber")
	private String primaryNumber;
	
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
	
	private Boolean presentAndPermanentAddressSame;
	
	@JsonProperty("attributes")
	private Map<String,Object> attributes;
	
	@Data
	public static class UserDetailFilter {
		
		@JsonProperty("firstName")
		private String firstName;
		
		@JsonProperty("lastName")
		private String lastName;
		
		@JsonProperty("email")
		private String email;
		
		@JsonProperty("phoneNumber")
		private String phoneNumber;
		
		@JsonProperty("userId")
		private String userId;
		
		@JsonProperty("publicFlag")
		private Boolean publicFlag;
		
		@JsonProperty("status")
		private List<ProfileStatus> status;
		
		@JsonProperty("roles")
		private List<RoleCode> roles;
		
		@JsonProperty("userByRole")
		private boolean userByRole;
	}
	
	@Data
	public static class UserAddress {
		@JsonProperty("ref")
		private String id;
		
		@JsonProperty("addressType")
		private AddressType addressType;
		
		@JsonProperty("addressLine1")
		private String addressLine1;
		
		@JsonProperty("addressLine2")
		private String addressLine2;
		
		@JsonProperty("addressLine3")
		private String addressLine3;
		
		@JsonProperty("hometown")
		private String hometown;
		
		@JsonProperty("state")
		private String state;
		
		@JsonProperty("district")
		private String district;
		
		@JsonProperty("country")
		private String country;
		
//		@JsonProperty("delete")
//		private boolean delete;
	}
	
	@Data
	public static class UserPhoneNumber {
		
		@JsonProperty("ref")
		private String id;
		
		@JsonProperty("phoneType")
		private PhoneType phoneType;
		
		@JsonProperty("phoneCode")
		private String phoneCode;
		
		@JsonProperty("phoneNumber")
		private String phoneNumber;
		
		@JsonProperty("displayNumber")
		private String displayNumber;
		
//		@JsonProperty("primary")
//		private boolean primary;
	//	
//		@JsonProperty("delete")
//		private boolean delete;
	}
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserRole {

		@JsonProperty("roleId")
		private String roleId;

		@JsonProperty("roleName")
		private String roleName;

		@JsonProperty("roleCode")
		private RoleCode roleCode;

//		@JsonProperty("roleGroup")
//		private RoleGroup roleGroup;

		@JsonProperty("description")
		private String description;
	}
	
	@Data
	public static class UserSocialMedia {
		@JsonProperty("ref")
		private String id;
		
		@JsonProperty("mediaType")
		private SocialMediaType mediaType;
		
		@JsonProperty("mediaName")
		private String mediaName;
		
		@JsonProperty("mediaIcon")
		private String mediaIcon;
		
		@JsonProperty("mediaLink")
		private String mediaLink;
		
//		@JsonProperty("delete")
//		private boolean delete;
	}

	@Deprecated
	@Data
	public class UserDetailUpdate {
		
		@JsonProperty("title")
		private String title;
		
		@JsonProperty("firstName")
		private String firstName;
		
		@JsonProperty("middleName")
		private String middleName;
		
		@JsonProperty("lastName")
		private String lastName;
		
		@JsonProperty("dateOfBirth")
		private Date dateOfBirth;
		
		@JsonProperty("gender")
		private String gender;
		
		@JsonProperty("about")
		private String about;
		
		@JsonProperty("base64Image")
		private String base64Image;
		
		@JsonProperty("removePicture")
		private boolean removePicture;
		
		@JsonProperty("addPicture")
		private boolean addPicture;
		
		@JsonProperty("phones")
		private List<UserPhoneNumber> phoneNumbers;
		
		@JsonProperty("addresses")
		private  List<UserAddress> addresses;
		
		@JsonProperty("socialMediaLinks")
		private List<UserSocialMedia> socialMediaLinks;



	}
	
}
