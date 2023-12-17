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
	private List<String> roleNames;

	private String email;
	private String primaryPhoneNumber;
	private List<PhoneDTO> phones;
	private List<AddressDTO> addresses;
	private List<EmailDTO> emails;
	private List<SocialMediaDTO> socialMedias;
	private String profileId;
	private List<String> userIds;
	private ProfileStatus status;
	
	private UserAdditionalDetailsDTO additionalDetails;

}
