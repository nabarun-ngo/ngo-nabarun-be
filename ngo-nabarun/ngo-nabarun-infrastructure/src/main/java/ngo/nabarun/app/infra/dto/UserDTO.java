package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.ProfileStatus;

@Data
public class UserDTO {
	
	//basic user details
	private String title;
	private String firstName;
	private String middleName;
	private String lastName;
	//this attribute will not be stored
	private String name;

	//more details
	private String imageUrl;
	private Date dateOfBirth;
	private String gender;
	private String about;
	private List<RoleDTO> roles;
	private String password;

	private String email;
	private String phoneNumber;
	private List<PhoneDTO> phones;
	private List<AddressDTO> addresses;
	private List<SocialMediaDTO> socialMedias;
	private String profileId;
	private String userId;
	private ProfileStatus status;
	private List<String> loginProviders;

	
	private UserAdditionalDetailsDTO additionalDetails;
	
	@Data	
	public static class UserDTOFilter{
		private String firstName;
		private String lastName;
		private String email;
		private String phoneNumber;
		private String profileId;
		private String userId;
		private List<ProfileStatus> status;
		private Boolean publicProfile;
		private Boolean deleted;


	}

}
