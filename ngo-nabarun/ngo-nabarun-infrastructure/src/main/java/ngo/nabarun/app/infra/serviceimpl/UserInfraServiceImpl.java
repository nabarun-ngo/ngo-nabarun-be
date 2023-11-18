package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ngo.nabarun.app.common.enums.DBContactType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.ext.service.IAuthManagementExtService;
import ngo.nabarun.app.infra.core.entity.UserContactEntity;
import ngo.nabarun.app.infra.core.entity.UserProfileEntity;
import ngo.nabarun.app.infra.core.repo.UserContactRepository;
import ngo.nabarun.app.infra.core.repo.UserProfileRepository;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.EmailDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.SocialMediaDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.InfraFieldHelper;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
public class UserInfraServiceImpl implements IUserInfraService {

	@Autowired
	private IAuthManagementExtService authManagementService;

	@Autowired
	private UserProfileRepository profileRepository;

	@Autowired
	private UserContactRepository userContactRepository;

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
		profile.setActiveContributor(userDTO.getAdditionalDetails().isActiveContributor());
		profile.setAvatarUrl(newAuth0User.getPicture());
		profile.setCreatedOn(newAuth0User.getCreatedAt());
		profile.setDeleted(false);
		profile.setDisplayPublic(userDTO.getAdditionalDetails().isDisplayPublic());
		profile.setTitle(userDTO.getTitle());
		profile.setEmail(newAuth0User.getEmail());
		profile.setFirstName(newAuth0User.getFirstName());
		profile.setLastName(newAuth0User.getLastName());
		profile.setMiddleName(userDTO.getMiddleName());
		profile.setProfileStatus(userDTO.getStatus().name());
		profile.setGender(userDTO.getGender());
		profile.setUserId(newAuth0User.getUserId());
		profile.setPhoneNumberString(userDTO.getPrimaryPhoneNumber());
		profile = profileRepository.save(profile);

		List<UserContactEntity> contact_list = new ArrayList<>();

		/**
		 * Saving phone numbers
		 */
		if (userDTO.getPhones() != null) {
			for (PhoneDTO userMobile : userDTO.getPhones()) {
				UserContactEntity contact_mobile = new UserContactEntity();
				contact_mobile.setContactType(DBContactType.PHONE);
				contact_mobile.setPhoneCode(userMobile.getPhoneCode());
				contact_mobile.setPhoneNumber(userMobile.getPhoneNumber());
				contact_mobile.setPhoneType(userMobile.getPhoneType().name());
				contact_mobile.setUser(profile);
				contact_mobile = userContactRepository.save(contact_mobile);
				contact_list.add(contact_mobile);
			}
		}

		/**
		 * Saving address
		 */
		if (userDTO.getAddresses() != null) {
			for (AddressDTO userAddress : userDTO.getAddresses()) {
				UserContactEntity contact_address = new UserContactEntity();
				contact_address.setContactType(DBContactType.ADDRESS);
				contact_address.setAddressType(userAddress.getAddressType().name());
				contact_address.setAddressLine(userAddress.getAddressLine());
				contact_address.setAddressHometown(userAddress.getHometown());
				contact_address.setAddressDistrict(userAddress.getDistrict());
				contact_address.setAddressState(userAddress.getState());
				contact_address.setAddressCountry(userAddress.getCountry());
				contact_address.setUser(profile);
				contact_address = userContactRepository.save(contact_address);
				contact_list.add(contact_address);
			}
		}

		/**
		 * Saving additional email
		 */
		if (userDTO.getEmails() != null) {
			for (EmailDTO userEmail : userDTO.getEmails()) {
				UserContactEntity contact_email = new UserContactEntity();
				contact_email.setContactType(DBContactType.EMAIL);
				contact_email.setEmailType(userEmail.getEmailType());
				contact_email.setEmailValue(userEmail.getEmail());
				contact_email.setUser(profile);
				contact_email = userContactRepository.save(contact_email);
				contact_list.add(contact_email);
			}
		}

		/**
		 * Saving social media
		 */
		if (userDTO.getSocialMedias() != null) {
			for (SocialMediaDTO userSocial : userDTO.getSocialMedias()) {
				UserContactEntity contact_social_media = new UserContactEntity();
				contact_social_media.setContactType(DBContactType.SOCIALMEDIA);
				contact_social_media.setSocialMediaType(userSocial.getSocialMediaType().name());
				contact_social_media.setSocialMediaName(userSocial.getSocialMediaName());
				contact_social_media.setSocialMediaURL(userSocial.getSocialMediaURL());
				contact_social_media.setUser(profile);
				contact_social_media = userContactRepository.save(contact_social_media);
				contact_list.add(contact_social_media);
			}
		}

		profile.setContacts(contact_list);
		/**
		 * Now converting newly saved details to UserDTO
		 */
		return InfraDTOHelper.convertToUserDTO(profile, newAuth0User);
	}

	@Override
	public void deleteUser(String profileId) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		authManagementService.deleteUser(profile.getUserId());
		profile.setProfileStatus(ProfileStatus.DELETED.name());
		profile.setDeleted(true);
		profile.setActiveContributor(false);
		profileRepository.save(profile);
	}

	@Override
	public List<UserDTO> getUsers(Integer page, Integer size, UserDTO filter) {
		List<UserProfileEntity> profiles = null;
		if (filter != null) {
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase()
					.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
			UserProfileEntity userFilter = new UserProfileEntity();
			userFilter.setFirstName(filter.getFirstName());
			userFilter.setLastName(filter.getLastName());
			userFilter.setEmail(filter.getEmail());
			// userFilter.setUserId(filter.getUserId());
			userFilter.setPhoneNumberString(filter.getPrimaryPhoneNumber());
			if (page == null || size == null) {
				profiles = profileRepository.findAll(Example.of(userFilter, matcher));
			} else {
				profiles = profileRepository.findAll(Example.of(userFilter, matcher), PageRequest.of(page, size))
						.getContent();
			}
		} else if (page != null && size != null) {
			profiles = profileRepository.findAll(PageRequest.of(page, size)).getContent();
		} else {
			profiles = profileRepository.findAll();
		}
		return profiles.stream().map(m -> InfraDTOHelper.convertToUserDTO(m, null)).collect(Collectors.toList());
	}

	@Override
	public List<UserDTO> getUsers() {
		return getUsers(null, null, null);
	}

	@Override
	public UserDTO getUserByProfileId(String profileId, boolean includeFullDetails) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		AuthUser auth0User = null;
		if (includeFullDetails) {
			auth0User = authManagementService.getUser(profile.getUserId());
		}
		return InfraDTOHelper.convertToUserDTO(profile, auth0User);
	}

	@Override
	public UserDTO getUserByUserId(String userId, boolean includeFullDetails) throws Exception {
		UserProfileEntity profile = profileRepository.findByUserIdContaining(userId).orElseThrow();
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

	@Override
	public String initiatePasswordReset(String userId, String appClientId, int expireInSec) throws Exception {
		return authManagementService.createPasswordResetTicket(userId, appClientId, expireInSec);
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
			updateAuthUser.setFullName(userDTO.getFirstName() + " " + profile.getLastName());
		} else if (userDTO.getLastName() != null) {
			updatedProfile.setLastName(userDTO.getLastName());
			updateAuthUser.setLastName(userDTO.getLastName());
			updateAuthUser.setFullName(profile.getFirstName() + " " + userDTO.getLastName());
		} else if (userDTO.getFirstName() != null && userDTO.getLastName() != null) {
			updatedProfile.setFirstName(userDTO.getFirstName());
			updateAuthUser.setFirstName(userDTO.getFirstName());
			updatedProfile.setLastName(userDTO.getLastName());
			updateAuthUser.setLastName(userDTO.getLastName());
			updateAuthUser.setFullName(userDTO.getFirstName() + " " + userDTO.getLastName());
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
		updatedProfile.setDisplayPublic(
				userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().isDisplayPublic() : null);
		updatedProfile.setProfileStatus(userDTO.getStatus() == null ? null : userDTO.getStatus().name());
		updatedProfile.setUserId(userDTO.getUserIds() == null ? null : InfraFieldHelper.stringListToString(userDTO.getUserIds()));

		updatedProfile.setAvatarUrl(userDTO.getImageUrl());
		CommonUtils.copyNonNullProperties(updatedProfile, profile);

		/**
		 * If First name or last name is not null then considering there is some changes
		 * to update
		 */
		if (userDTO.getFirstName() != null || userDTO.getLastName() != null || userDTO.getEmail() != null) {
			updateAuthUser = authManagementService.updateUser(profile.getUserId(), updateAuthUser);
		}

		profile.setContacts(null);
		profile = profileRepository.save(profile);

		List<UserContactEntity> contact_list = new ArrayList<UserContactEntity>();
		/**
		 * Updating phone numbers
		 */
		if (userDTO.getPhones() != null) {
			for (PhoneDTO userMobile : userDTO.getPhones()) {
				if (userMobile.isDelete()) {
					userContactRepository.deleteById(userMobile.getId());
				} else if (userMobile.getId() == null) {
					UserContactEntity contact_mobile = new UserContactEntity();
					contact_mobile.setContactType(DBContactType.PHONE);
					contact_mobile.setPhoneCode(userMobile.getPhoneCode());
					contact_mobile.setPhoneNumber(userMobile.getPhoneNumber());
					contact_mobile.setPhoneType(userMobile.getPhoneType().name());
					contact_mobile.setUser(profile);
					contact_mobile = userContactRepository.save(contact_mobile);
					contact_list.add(contact_mobile);
				} else {
					UserContactEntity contact_mobile = userContactRepository.findById(userMobile.getId()).orElseThrow();
					UserContactEntity contact_mobile_updated = new UserContactEntity();
					contact_mobile_updated.setPhoneCode(userMobile.getPhoneCode());
					contact_mobile_updated.setPhoneNumber(userMobile.getPhoneNumber());
					contact_mobile_updated
							.setPhoneType(userMobile.getPhoneType() == null ? null : userMobile.getPhoneType().name());
					CommonUtils.copyNonNullProperties(contact_mobile_updated, contact_mobile);
					contact_mobile = userContactRepository.save(contact_mobile);
					contact_list.add(contact_mobile);
				}
			}
		}

		/**
		 * Saving address
		 */
		if (userDTO.getAddresses() != null) {
			for (AddressDTO userAddress : userDTO.getAddresses()) {
				if (userAddress.isDelete()) {
					userContactRepository.deleteById(userAddress.getId());
				} else if (userAddress.getId() == null) {
					UserContactEntity contact_address = new UserContactEntity();
					contact_address.setContactType(DBContactType.ADDRESS);
					contact_address.setAddressType(userAddress.getAddressType().name());
					contact_address.setAddressLine(userAddress.getAddressLine());
					contact_address.setAddressHometown(userAddress.getHometown());
					contact_address.setAddressDistrict(userAddress.getDistrict());
					contact_address.setAddressState(userAddress.getState());
					contact_address.setAddressCountry(userAddress.getCountry());
					contact_address.setUser(profile);
					contact_address = userContactRepository.save(contact_address);
					contact_list.add(contact_address);
				} else {
					UserContactEntity contact_address = userContactRepository.findById(userAddress.getId())
							.orElseThrow();
					UserContactEntity contact_address_updated = new UserContactEntity();
					contact_address_updated.setAddressType(
							userAddress.getAddressType() == null ? null : userAddress.getAddressType().name());
					contact_address_updated.setAddressLine(userAddress.getAddressLine());
					contact_address_updated.setAddressHometown(userAddress.getHometown());
					contact_address_updated.setAddressDistrict(userAddress.getDistrict());
					contact_address_updated.setAddressState(userAddress.getState());
					contact_address_updated.setAddressCountry(userAddress.getCountry());
					CommonUtils.copyNonNullProperties(contact_address_updated, contact_address);
					contact_address = userContactRepository.save(contact_address);
					contact_list.add(contact_address);
				}
			}
		}

		/**
		 * Saving social media
		 */
		if (userDTO.getSocialMedias() != null) {
			for (SocialMediaDTO userSocial : userDTO.getSocialMedias()) {
				if (userSocial.isDelete()) {
					userContactRepository.deleteById(userSocial.getId());
				} else if (userSocial.getId() == null) {
					UserContactEntity contact_social_media = new UserContactEntity();
					contact_social_media.setContactType(DBContactType.SOCIALMEDIA);
					contact_social_media.setSocialMediaType(userSocial.getSocialMediaType().name());
					contact_social_media.setSocialMediaName(userSocial.getSocialMediaName());
					contact_social_media.setSocialMediaURL(userSocial.getSocialMediaURL());
					contact_social_media.setUser(profile);
					contact_social_media = userContactRepository.save(contact_social_media);
					contact_list.add(contact_social_media);
				} else {
					UserContactEntity contact_social_media = userContactRepository.findById(userSocial.getId())
							.orElseThrow();
					UserContactEntity contact_social_media_updated = new UserContactEntity();
					contact_social_media_updated.setSocialMediaType(userSocial.getSocialMediaType().name());
					contact_social_media_updated.setSocialMediaName(userSocial.getSocialMediaName());
					contact_social_media_updated.setSocialMediaURL(userSocial.getSocialMediaURL());
					CommonUtils.copyNonNullProperties(contact_social_media_updated, contact_social_media);
					contact_social_media = userContactRepository.save(contact_social_media);
					contact_list.add(contact_social_media);
				}
			}
		}

		profile.setContacts(contact_list);
		return InfraDTOHelper.convertToUserDTO(profile, updateAuthUser);
	}

	@Override
	public List<RoleDTO> getUserRoles(String profileId) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		List<String> authUserIds=InfraFieldHelper.stringToStringList(profile.getUserId());
		List<AuthUserRole> authUserRoles= new ArrayList<>();
		for(String authUserId:authUserIds) {
			authUserRoles.addAll(authManagementService.getRoles(authUserId));
		}
		authUserRoles=authUserRoles.stream().filter(CommonUtils.distinctByKey(AuthUserRole::getRoleId)).toList();
		return authUserRoles.stream().map(m -> InfraDTOHelper.convertToRoleDTO(m)).toList();
	}

	@Override
	public long getUsersCount() {
		return profileRepository.count();
	}

	@Override
	public void deleteRolesFromUser(String profileId, List<RoleDTO> roles) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		List<String> authUserIds=InfraFieldHelper.stringToStringList(profile.getUserId());
		for(String authUserId:authUserIds) {
			authManagementService.removeRolesFromUser(authUserId, roles.stream().map(m->m.getId()).toList());
		}
		List<String> roleNames=InfraFieldHelper.stringToStringList(profile.getRoleString());
		for(RoleDTO role : roles) {
			roleNames.remove(role.getDisplayName());
		}
		profile.setRoleString(InfraFieldHelper.stringListToString(roleNames));
		profileRepository.save(profile);
	}

	@Override
	public void addRolesToUser(String profileId, List<RoleDTO> roles) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		List<String> authUserIds=InfraFieldHelper.stringToStringList(profile.getUserId());
		for(String authUserId:authUserIds) {
			authManagementService.assignRolesToUser(authUserId, roles.stream().map(m->m.getId()).toList());
		}
		List<String> roleNames=InfraFieldHelper.stringToStringList(profile.getRoleString());
		for(RoleDTO role : roles) {
			roleNames.add(role.getDisplayName());
		}
		profile.setRoleString(InfraFieldHelper.stringListToString(roleNames));
		profileRepository.save(profile);
	}

	@Override
	public void assignUsersToRole(String roleId, List<UserDTO> users) throws Exception {
		// TODO Auto-generated method stub
		
	}


}
