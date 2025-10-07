package ngo.nabarun.domain.user.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import static ngo.nabarun.common.util.CommonUtil.*;

import ngo.nabarun.domain.user.enums.LoginMethod;
import ngo.nabarun.domain.user.enums.UserStatus;

@Getter
@Builder
@AllArgsConstructor
public class User {

	private final String id;
	private String title;
	private String firstName;
	private String middleName;
	private String lastName;
	private Date dateOfBirth;
	private String gender;
	private String about;
	private String picture;
	private List<Role> roles;
	private String email;
	private PhoneNumber primaryNumber;
	private PhoneNumber secondaryNumber;
	private Address presentAddress;
	private Address permanentAddress;
	private List<Link> socialMediaLinks;
	private final Date createdOn;
	private boolean activeDonor;
	private boolean publicProfile;
	private String userId;
	private UserStatus status;
	private List<LoginMethod> loginMethod;
	private Boolean addressSame;
	private Boolean profileCompleted;

	// Constructor
	public User(UserCreate detail) {
		this.id = UUID.randomUUID().toString();
		this.firstName = detail.getFirstName();
		this.lastName = detail.getLastName();
		this.email = detail.getEmail();
		this.status = UserStatus.DRAFT;
		this.createdOn = new Date();
		this.profileCompleted = false;
		this.activeDonor = true;
		this.picture = "https://ui-avatars.com/api/?name=" + this.firstName + "+" + this.lastName
				+ "&background=random";
		this.roles = new ArrayList<Role>();
		if(detail.getDefaultRoles() != null) {
			this.roles.addAll(detail.getDefaultRoles());
		}
	}

	public String getFullName() {
		return (firstName + " " + lastName).trim();
	}

	public String getInitials() {
		return (this.firstName.substring(0, 1) + this.lastName.substring(0, 1)).trim().toUpperCase();
	}

	// Domain behaviors

	/**
	 * Update self-editable fields
	 */
	public void updateUser(UserSelfUpdate detail) {
		this.title = defaultIfNull(detail.getTitle(), this.title);
		this.firstName = defaultIfNull(detail.getFirstName(), this.firstName);
		this.middleName = defaultIfNull(detail.getMiddleName(), this.middleName);
		this.lastName = defaultIfNull(detail.getLastName(), this.lastName);
		this.dateOfBirth = defaultIfNull(detail.getDateOfBirth(), this.dateOfBirth);
		this.gender = defaultIfNull(detail.getGender(), this.gender);
		this.about = defaultIfNull(detail.getAbout(), this.about);
		this.picture = defaultIfNull(detail.getPicture(), this.picture);
		this.primaryNumber = defaultIfNull(detail.getPrimaryNumber(), this.primaryNumber);
		this.secondaryNumber = defaultIfNull(detail.getSecondaryNumber(), this.secondaryNumber);
		this.presentAddress = defaultIfNull(detail.getPresentAddress(), this.presentAddress);
		this.permanentAddress = defaultIfNull(detail.getPermanentAddress(), this.permanentAddress);
		this.addressSame = defaultIfNull(detail.getIsAddressSame(), this.addressSame);
		this.publicProfile = defaultIfNull(detail.getIsPublicProfile(), this.isPublicProfile());
		this.socialMediaLinks = defaultIfNull(detail.getSocialMediaLinks(), this.socialMediaLinks);
		this.profileCompleted = isNotNull(this.firstName, this.lastName, this.dateOfBirth, this.gender, this.email,
				this.userId);
	}

	/**
	 * Update admin-only fields
	 */
	public void updateUser(UserUpdate detail) {
		this.roles = defaultIfNull(detail.getRoles(), this.roles);
		this.status = defaultIfNull(detail.getStatus(), this.status);
		this.activeDonor = defaultIfNull(detail.getIsActiveDonor(), this.activeDonor);
		this.userId = defaultIfNull(detail.getUserId(), this.userId);
		this.loginMethod = defaultIfNull(detail.getLoginMethod(), this.loginMethod);
	}

	@Data
	public static class UserSelfUpdate {
		private String title;
		private String firstName;
		private String middleName;
		private String lastName;
		private Date dateOfBirth;
		private String gender;
		private String about;
		private String picture;
		private PhoneNumber primaryNumber;
		private PhoneNumber secondaryNumber;
		private Address presentAddress;
		private Address permanentAddress;
		private List<Link> socialMediaLinks;
		private Boolean isPublicProfile;
		private Boolean isAddressSame;
	}

	@Data
	public static class UserUpdate {
		private List<Role> roles;
		private Boolean isActiveDonor;
		private String userId;
		private UserStatus status;
		private List<LoginMethod> loginMethod;
	}

	@Data
	public static class UserCreate {
		private String firstName;
		private String lastName;
		private String email;
		private PhoneNumber number;
		private List<Role> defaultRoles;
	}
}
