package ngo.nabarun.domain.user.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import static ngo.nabarun.common.util.CommonUtil.*;

import ngo.nabarun.common.util.PasswordUtil;
import ngo.nabarun.domain.BusinessException;
import ngo.nabarun.domain.user.enums.LoginMethod;
import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.vo.AdminUpdate;
import ngo.nabarun.domain.user.vo.ProfileUpdate;
import ngo.nabarun.domain.user.vo.RegistrationDetail;
import ngo.nabarun.domain.user.vo.RoleUpdateResult;

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
	private Boolean blocked;
	private String password;

	// Factory-style constructor with basic invariants
	public User(RegistrationDetail detail) {
		if (detail == null)
			throw new BusinessException("UserCreate cannot be null");
		if (isNullOrEmpty(detail.firstName()) || isNullOrEmpty(detail.lastName()) || isNullOrEmpty(detail.email())) {
			throw new BusinessException("firstName, lastName and email are required");
		}
		this.id = UUID.randomUUID().toString();
		this.firstName = detail.firstName().trim();
		this.lastName = detail.lastName().trim();
		this.email = detail.email().trim();
		this.primaryNumber = detail.number();
		this.status = UserStatus.DRAFT;
		this.createdOn = new Date();
		this.profileCompleted = false;
		this.activeDonor = true;
		this.picture = "https://ui-avatars.com/api/?name=" + this.firstName + "+" + this.lastName
				+ "&background=random";
		this.roles = new ArrayList<>();
		this.socialMediaLinks = new ArrayList<>();
		this.loginMethod = List.of(LoginMethod.EMAIL, LoginMethod.PASSWORD);
		this.password = PasswordUtil.generateStrongPassword(8);
	}

	public String getFullName() {
		String f = firstName == null ? "" : firstName;
		String l = lastName == null ? "" : lastName;
		return (f + " " + l).trim();
	}

	public String getInitials() {
		if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName))
			return "";
		return (this.firstName.substring(0, 1) + this.lastName.substring(0, 1)).trim().toUpperCase();
	}

	// Convenience immutable read-only views
	public List<Role> getRoles() {
		return roles == null ? Collections.emptyList() : Collections.unmodifiableList(roles);
	}

	public List<Link> getSocialMediaLinks() {
		return socialMediaLinks == null ? Collections.emptyList() : Collections.unmodifiableList(socialMediaLinks);
	}

	public List<LoginMethod> getLoginMethod() {
		return loginMethod == null ? Collections.emptyList() : Collections.unmodifiableList(loginMethod);
	}

	// Domain behaviors

	/**
	 * Update self-editable fields
	 * @return 
	 */
	public ProfileUpdate updateUser(ProfileUpdate detail) {
		this.title = defaultIfNull(detail.title(), this.title);
		this.firstName = defaultIfNull(detail.firstName(), this.firstName);
		this.middleName = defaultIfNull(detail.middleName(), this.middleName);
		this.lastName = defaultIfNull(detail.lastName(), this.lastName);
		this.dateOfBirth = defaultIfNull(detail.dateOfBirth(), this.dateOfBirth);
		this.gender = defaultIfNull(detail.gender(), this.gender);
		this.about = defaultIfNull(detail.about(), this.about);
		this.picture = defaultIfNull(detail.picture(), this.picture);
		this.primaryNumber = defaultIfNull(detail.primaryNumber(), this.primaryNumber);
		this.secondaryNumber = defaultIfNull(detail.secondaryNumber(), this.secondaryNumber);
		this.presentAddress = defaultIfNull(detail.presentAddress(), this.presentAddress);
		this.permanentAddress = defaultIfNull(detail.permanentAddress(), this.permanentAddress);
		this.addressSame = defaultIfNull(detail.isAddressSame(), this.addressSame);
		this.publicProfile = defaultIfNull(detail.isPublicProfile(), this.isPublicProfile());
		this.socialMediaLinks = defaultIfNull(detail.socialMediaLinks(), this.socialMediaLinks);
		this.profileCompleted = isNotNull(this.firstName, this.lastName, this.dateOfBirth, this.gender, this.email,
				this.userId);
		return detail;
	}

	/**
	 * Update admin-only fields
	 * @return 
	 */
	public AdminUpdate updateUser(AdminUpdate detail) {
		this.roles = defaultIfNull(detail.roles(), this.roles);
		this.status = defaultIfNull(detail.status(), this.status);
		this.activeDonor = defaultIfNull(detail.isActiveDonor(), this.activeDonor);
		this.userId = defaultIfNull(detail.userId(), this.userId);
		this.loginMethod = defaultIfNull(detail.loginMethod(), this.loginMethod);
		return detail;
	}

	 /**
     * Update roles (with business rules)
     * @param newRoles roles selected by admin or system
     * @param defaultRoles default roles that must always exist
     */
    public RoleUpdateResult updateRoles(List<Role> newRoles, List<Role> defaultRoles) {
        if (newRoles == null) return RoleUpdateResult.empty();

        List<Role> currentRoles = new ArrayList<>(roles);
        List<Role> incomingRoles = new ArrayList<>(newRoles);

        // Ensure default roles are always present
        addMissingDefaultRolesTo(incomingRoles, defaultRoles);

        // Compute diff
        List<Role> toAdd = incomingRoles.stream()
                .filter(r -> currentRoles.stream().noneMatch(cr -> Objects.equals(cr.roleCode(), r.roleCode())))
                .toList();

        List<Role> toRemove = currentRoles.stream()
                .filter(r -> incomingRoles.stream().noneMatch(ir -> Objects.equals(ir.roleCode(), r.roleCode())))
                .filter(r -> defaultRoles.stream().noneMatch(dr -> Objects.equals(dr.roleCode(), r.roleCode())))
                .toList();

        this.roles.clear();
        this.roles.addAll(incomingRoles);

        return new RoleUpdateResult(toAdd, toRemove);
    }

    private void addMissingDefaultRolesTo(List<Role> roles, List<Role> defaultRoles) {
        if (defaultRoles == null || defaultRoles.isEmpty()) return;
        for (Role dr : defaultRoles) {
            boolean exists = roles.stream().anyMatch(r -> Objects.equals(r.roleCode(), dr.roleCode()));
            if (!exists) roles.add(dr);
        }
    }

	public void changeStatus(UserStatus newStatus) {
		if (newStatus == null)
			return;
		this.status = newStatus;
	}
}
