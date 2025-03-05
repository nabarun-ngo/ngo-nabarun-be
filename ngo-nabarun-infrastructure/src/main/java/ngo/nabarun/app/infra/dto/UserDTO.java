package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import ngo.nabarun.app.common.enums.LoginMethod;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;

@Data
public class UserDTO {

	// basic user details
	private String title;
	private String firstName;
	private String middleName;
	private String lastName;
	// this attribute will not be stored
	private String name;

	// more details
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
	private List<LoginMethod> loginProviders;
	private Boolean presentPermanentSame;

	private UserAdditionalDetailsDTO additionalDetails;
	private Map<String,Object> attributes;

	@Data
	public static class UserDTOFilter {
		private String firstName;
		private String lastName;
		private String email;
		private String phoneNumber;
		private String profileId;
		private String userId;
		private List<ProfileStatus> status;
		private Boolean publicProfile;
		private Boolean deleted;
		private List<RoleCode> roles;

	}

	public Map<String, Object> toMap(Map<String, String> domainKeyValues) {
		Map<String, Object> user = new HashMap<>();
		user.put("firstName", firstName);
		user.put("lastName", lastName);
		user.put("name", name);
		user.put("email", email);
		user.put("roles",
				roles == null ? null
						: roles.stream().filter(f -> f.getCode() != null)
								.map(m -> domainKeyValues.get(m.getCode().name())).collect(Collectors.toList())
								.toString());
		user.put("phoneNumber", phoneNumber);
		return user;
	}

}
