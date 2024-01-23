package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.querydsl.core.BooleanBuilder;

import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.ext.service.IAuthManagementExtService;
import ngo.nabarun.app.infra.core.entity.QUserProfileEntity;
import ngo.nabarun.app.infra.core.entity.UserProfileEntity;
import ngo.nabarun.app.infra.core.repo.UserProfileRepository;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.SocialMediaDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.UserDTO.UserDTOFilter;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.InfraFieldHelper;
import ngo.nabarun.app.infra.misc.WhereClause;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
public class UserInfraServiceImpl implements IUserInfraService {

	@Autowired
	private IAuthManagementExtService authManagementService;

	@Autowired
	private UserProfileRepository profileRepository;

//	@Autowired
//	private CustomFieldRepository customFieldRepository;

	@Override
	public Page<UserDTO> getUsers(Integer page, Integer size, UserDTOFilter filter) {
		Page<UserProfileEntity> profiles = null;
		if (filter != null) {
			QUserProfileEntity qUser = QUserProfileEntity.userProfileEntity;
			BooleanBuilder query = WhereClause.builder()
					.optionalAnd(filter.getEmail() != null, () -> qUser.email.eq(filter.getEmail()))
					.optionalAnd(filter.getFirstName() != null, () -> qUser.firstName.eq(filter.getFirstName()))
					.optionalAnd(filter.getLastName() != null, () -> qUser.lastName.eq(filter.getLastName()))
					.optionalAnd(filter.getPhoneNumber() != null,
							() -> qUser.phoneNumber.contains(filter.getPhoneNumber())
									.or(qUser.altPhoneNumber.contains(filter.getPhoneNumber())))
					.optionalAnd(filter.getProfileId() != null, () -> qUser.id.eq(filter.getProfileId()))
					.optionalAnd(filter.getStatus() != null, () -> qUser.status.eq(filter.getStatus().name()))
					.optionalAnd(filter.getUserId() != null, () -> qUser.userId.eq(filter.getUserId())).build();

			if (page == null || size == null) {
				List<UserProfileEntity> result = new ArrayList<>();
				profileRepository.findAll(query).iterator().forEachRemaining(result::add);
				profiles = new PageImpl<>(result);
			} else {
				profiles = profileRepository.findAll(query, PageRequest.of(page, size));
			}
		} else if (page != null && size != null) {
			profiles = profileRepository.findAll(PageRequest.of(page, size));
		} else {
			profiles = new PageImpl<>(profileRepository.findAll());
		}
		return profiles.map(m -> InfraDTOHelper.convertToUserDTO(m, null));
	}

	@Override
	public UserDTO getUser(String id, boolean includeFullDetails) throws Exception {
		UserProfileEntity profile = profileRepository.findById(id).orElseThrow();
		AuthUser auth0User = null;
		if (includeFullDetails) {
			auth0User = authManagementService.getUser(profile.getUserId());
		}
		return InfraDTOHelper.convertToUserDTO(profile, auth0User);
	}

	@Override
	public UserDTO getUserByUserId(String userId, boolean includeFullDetails) throws Exception {
		UserProfileEntity profile = profileRepository.findByUserId(userId).orElseThrow();
		AuthUser auth0User = null;
		if (includeFullDetails) {
			auth0User = authManagementService.getUser(profile.getUserId());
		}
		return InfraDTOHelper.convertToUserDTO(profile, auth0User);
	}

	@Override
	public UserDTO getUserByEmail(String email, boolean includeFullDetails) throws Exception {
		UserProfileEntity profile = profileRepository.findByEmail(email).orElseThrow();
		AuthUser auth0User = null;
		if (includeFullDetails) {
			auth0User = authManagementService.getUser(profile.getUserId());
		}
		return InfraDTOHelper.convertToUserDTO(profile, auth0User);
	}

	/**
	 * Creating user in auth0 system auth0 system will throw an error if user exists
	 * with email
	 * 
	 * Check if any profile available with the given email id if available mark that
	 * profile as non deleted else create profile and update auth0 user id.
	 * 
	 * profile status , display public this 2 fields must be passed from business
	 * layer
	 */
	@Override
	public UserDTO createUser(UserDTO userDTO) throws Exception {
		Assert.notNull(userDTO.getAdditionalDetails(), "additionalDetails object must not be null.");

		/**
		 * creating user in auth0 system auth0 system will throw an error if user exists
		 * with email this will be hard blocker as for re joiners auth0 profile must be
		 * deleted during their termination
		 */

		AuthUser newAuth0User = new AuthUser();
		newAuth0User.setFirstName(userDTO.getFirstName());
		newAuth0User.setLastName(userDTO.getLastName());
		newAuth0User.setEmail(userDTO.getEmail());
		newAuth0User.setPassword(PasswordUtils.generateStrongPassword(20));
		newAuth0User.setEmailVerified(
				userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().isEmailVerified() : false);
		newAuth0User = authManagementService.createUser(newAuth0User);

		/**
		 * Checking if user exists with the email if not available Creating user profile
		 * in local DB if available updating with latest details available System will
		 * throw error if creation is happening with existing records
		 * 
		 */
		Optional<UserProfileEntity> userProfile = profileRepository.findByEmail(userDTO.getEmail());
		UserProfileEntity profile = userProfile.isPresent() ? userProfile.get() : new UserProfileEntity();
		profile.setTitle(userDTO.getTitle());
		profile.setFirstName(newAuth0User.getFirstName());
		profile.setMiddleName(userDTO.getMiddleName());
		profile.setLastName(newAuth0User.getLastName());
		profile.setAvatarUrl(newAuth0User.getPicture());
		profile.setDateOfBirth(userDTO.getDateOfBirth());
		profile.setGender(userDTO.getGender());
		profile.setEmail(newAuth0User.getEmail());
		profile.setAbout(userDTO.getAbout());
		profile.setCreatedBy(null);//
		profile.setActiveContributor(
				userDTO.getAdditionalDetails() == null ? null : userDTO.getAdditionalDetails().isActiveContributor());
		profile.setCreatedOn(newAuth0User.getCreatedAt());
		profile.setDeleted(false);
		profile.setPublicProfile(userDTO.getAdditionalDetails().isDisplayPublic());
		profile.setStatus(userDTO.getStatus().name());
		profile.setUserId(newAuth0User.getUserId());

		if (userDTO.getAddresses() != null) {
			for (AddressDTO addressDTO : userDTO.getAddresses()) {
				switch (addressDTO.getAddressType()) {
				case PRESENT:
					profile.setAddressLine1(addressDTO.getAddressLine1());
					profile.setAddressLine2(addressDTO.getAddressLine2());
					profile.setAddressLine3(addressDTO.getAddressLine3());
					profile.setHometown(addressDTO.getHometown());
					profile.setDistrict(addressDTO.getDistrict());
					profile.setState(addressDTO.getState());
					profile.setCountry(addressDTO.getCountry());
					break;
				case PERMANENT:

					break;
				default:
					break;
				}
			}
		}

		if (userDTO.getPhones() != null) {
			for (PhoneDTO userMobile : userDTO.getPhones()) {
				switch (userMobile.getPhoneType()) {
				case PRIMARY:
					profile.setPhoneNumber(InfraFieldHelper
							.stringListToString(List.of(userMobile.getPhoneCode(), userMobile.getPhoneNumber()), "-"));
					break;
				case ALTERNATIVE:
					profile.setAltPhoneNumber(InfraFieldHelper
							.stringListToString(List.of(userMobile.getPhoneCode(), userMobile.getPhoneNumber()), "-"));
					break;
				default:
					break;
				}
			}
		}

		if (userDTO.getSocialMedias() != null) {
			for (SocialMediaDTO userSocial : userDTO.getSocialMedias()) {
				switch (userSocial.getSocialMediaType()) {
				case FACEBOOK:
					profile.setFacebookLink(userSocial.getSocialMediaURL());
					break;
				case INSTAGRAM:
					profile.setInstagramLink(userSocial.getSocialMediaURL());
					break;
				case TWITTER:
					profile.setTwitterLink(userSocial.getSocialMediaURL());
					break;
				case WHATSAPP:
					profile.setWhatsappLink(userSocial.getSocialMediaURL());
					break;
				case LINKEDIN:
					profile.setLinkedInLink(userSocial.getSocialMediaURL());
					break;
				default:
					break;
				}
			}
		}

		profile = profileRepository.save(profile);
		return InfraDTOHelper.convertToUserDTO(profile, newAuth0User);
	}

	@Override
	public UserDTO updateUser(String profileId, UserDTO userDTO) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		AuthUser updateAuthUser = new AuthUser();
		updateAuthUser.setFirstName(userDTO.getFirstName() == null ? profile.getFirstName() : userDTO.getFirstName());
		updateAuthUser.setLastName(userDTO.getLastName() == null ? profile.getLastName() : userDTO.getLastName());

		UserProfileEntity updatedProfile = new UserProfileEntity();
		updatedProfile.setTitle(userDTO.getTitle());
		if (userDTO.getFirstName() != null) {
			updatedProfile.setFirstName(userDTO.getFirstName());
			updateAuthUser.setFirstName(userDTO.getFirstName());
			updateAuthUser.setFullName(userDTO.getName() == null ? userDTO.getFirstName() + " " + profile.getLastName()
					: userDTO.getName());
		} else if (userDTO.getLastName() != null) {
			updatedProfile.setLastName(userDTO.getLastName());
			updateAuthUser.setLastName(userDTO.getLastName());
			updateAuthUser.setFullName(userDTO.getName() == null ? userDTO.getFirstName() + " " + profile.getLastName()
					: userDTO.getName());
		} else if (userDTO.getFirstName() != null && userDTO.getLastName() != null) {
			updatedProfile.setFirstName(userDTO.getFirstName());
			updateAuthUser.setFirstName(userDTO.getFirstName());
			updatedProfile.setLastName(userDTO.getLastName());
			updateAuthUser.setLastName(userDTO.getLastName());
			updateAuthUser.setFullName(userDTO.getName() == null ? userDTO.getFirstName() + " " + profile.getLastName()
					: userDTO.getName());
		}
		updatedProfile.setMiddleName(userDTO.getMiddleName());
		updatedProfile.setAbout(userDTO.getAbout());
		updatedProfile.setDateOfBirth(userDTO.getDateOfBirth());
		updatedProfile.setGender(userDTO.getGender());
		if (userDTO.getEmail() != null) {
			updatedProfile.setEmail(userDTO.getEmail());
			updateAuthUser.setEmail(userDTO.getEmail());
		}

		updatedProfile.setActiveContributor(
				userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().isActiveContributor() : null);
		updatedProfile.setPublicProfile(
				userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().isDisplayPublic() : null);
		updatedProfile.setStatus(userDTO.getStatus() == null ? null : userDTO.getStatus().name());
		updatedProfile.setUserId(userDTO.getUserId() == null ? null : userDTO.getUserId());
		updatedProfile.setAvatarUrl(userDTO.getImageUrl());

		if (userDTO.getAddresses() != null) {
			for (AddressDTO addressDTO : userDTO.getAddresses()) {
				switch (addressDTO.getAddressType()) {
				case PRESENT:
					updatedProfile.setAddressLine1(addressDTO.getAddressLine1());
					updatedProfile.setAddressLine2(addressDTO.getAddressLine2());
					updatedProfile.setAddressLine3(addressDTO.getAddressLine3());
					updatedProfile.setHometown(addressDTO.getHometown());
					updatedProfile.setDistrict(addressDTO.getDistrict());
					updatedProfile.setState(addressDTO.getState());
					updatedProfile.setCountry(addressDTO.getCountry());
					break;
				case PERMANENT:

					break;
				default:
					break;
				}
			}
		}

		if (userDTO.getPhones() != null) {
			for (PhoneDTO userMobile : userDTO.getPhones()) {
				switch (userMobile.getPhoneType()) {
				case PRIMARY:
					updatedProfile.setPhoneNumber(InfraFieldHelper
							.stringListToString(List.of(userMobile.getPhoneCode(), userMobile.getPhoneNumber()), "-"));
					break;
				case ALTERNATIVE:
					updatedProfile.setAltPhoneNumber(InfraFieldHelper
							.stringListToString(List.of(userMobile.getPhoneCode(), userMobile.getPhoneNumber()), "-"));
					break;
				default:
					break;
				}
			}
		}

		if (userDTO.getSocialMedias() != null) {
			for (SocialMediaDTO userSocial : userDTO.getSocialMedias()) {
				switch (userSocial.getSocialMediaType()) {
				case FACEBOOK:
					updatedProfile.setFacebookLink(userSocial.getSocialMediaURL());
					break;
				case INSTAGRAM:
					updatedProfile.setInstagramLink(userSocial.getSocialMediaURL());
					break;
				case TWITTER:
					updatedProfile.setTwitterLink(userSocial.getSocialMediaURL());
					break;
				case WHATSAPP:
					updatedProfile.setWhatsappLink(userSocial.getSocialMediaURL());
					break;
				case LINKEDIN:
					updatedProfile.setLinkedInLink(userSocial.getSocialMediaURL());
					break;
				default:
					break;
				}
			}
		}

		/**
		 * Updating roles
		 */

		CommonUtils.copyNonNullProperties(updatedProfile, profile);

		/**
		 * If First name or last name is not null then considering there is some changes
		 * to update
		 */
		if (userDTO.getFirstName() != null || userDTO.getLastName() != null || userDTO.getEmail() != null) {
			updateAuthUser = authManagementService.updateUser(profile.getUserId(), updateAuthUser);
		}

		profile = profileRepository.save(profile);

		return InfraDTOHelper.convertToUserDTO(profile, updateAuthUser);
	}

	@Override
	public void deleteUser(String profileId) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		authManagementService.deleteUser(profile.getUserId());
		profile.setStatus(ProfileStatus.DELETED.name());
		profile.setDeleted(true);
		profile.setActiveContributor(false);
		profileRepository.save(profile);
	}

	@Override
	public String initiatePasswordReset(String userId, String appClientId, int expireInSec) throws Exception {
		return authManagementService.createPasswordResetTicket(userId, appClientId, expireInSec);
	}

	@Override
	public List<RoleDTO> getUserRoles(String profileId) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		List<AuthUserRole> authUserRoles = authManagementService.getRoles(profile.getUserId());
		return authUserRoles.stream().map(m -> InfraDTOHelper.convertToRoleDTO(m)).toList();
	}

	@Override
	public void deleteRolesFromUser(String profileId, List<RoleDTO> roles) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
//		for (String authUserId : authUserIds) {
//			authManagementService.removeRolesFromUser(authUserId, roles.stream().map(m -> m.getId()).toList());
//		}
//		List<String> roleNames = InfraFieldHelper.stringToStringList(profile.getRoleString());
//		for (RoleDTO role : roles) {
//			roleNames.remove(role.getDisplayName());
//		}
//		profile.setRoleString(InfraFieldHelper.stringListToString(roleNames));
		profileRepository.save(profile);
	}

	@Override
	public void addRolesToUser(String profileId, List<RoleDTO> roles) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		List<String> authUserIds = InfraFieldHelper.stringToStringList(profile.getUserId());
		for (String authUserId : authUserIds) {
			authManagementService.assignRolesToUser(authUserId, roles.stream().map(m -> m.getId()).toList());
		}
//		List<String> roleNames = InfraFieldHelper.stringToStringList(profile.getRoleString());
//		for (RoleDTO role : roles) {
//			roleNames.add(role.getDisplayName());
//		}
		// profile.setRoleString(InfraFieldHelper.stringListToString(roleNames));
		profileRepository.save(profile);
	}

	@Override
	public void assignUsersToRole(String roleId, List<UserDTO> users) throws Exception {

	}

	@Override
	public long getUsersCount() {
		return 0;
	}

}
