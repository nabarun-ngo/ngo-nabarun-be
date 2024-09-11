package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.AuthConnection;
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

@Slf4j
@Service
public class UserInfraServiceImpl implements IUserInfraService {

	@Autowired
	private IAuthManagementExtService authManagementService;

	@Autowired
	private UserProfileRepository profileRepository;

//	@Autowired
//	private UserRoleRepository roleRepository;

	private static Map<String, String> roleVsIdMap = new HashMap<>();

	private String getRoleId(String code) {
		if (roleVsIdMap.isEmpty()) {
			try {
				for (AuthUserRole role : authManagementService.getAllAvailableRoles()) {
					roleVsIdMap.put(role.getRoleName(), role.getRoleId());
				}
			} catch (ThirdPartyException e) {
				e.printStackTrace();
			}
		}
		return roleVsIdMap.get(code);
	}

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
					.optionalAnd(filter.getStatus() != null,
							() -> qUser.status.in(filter.getStatus().stream().map(m -> m.name()).toList()))
					.optionalAnd(filter.getPublicProfile() != null,
							() -> qUser.publicProfile.eq(filter.getPublicProfile()))
					.optionalAnd(filter.getDeleted() != null, () -> qUser.deleted.eq(filter.getDeleted()))
					.optionalAnd(filter.getUserId() != null, () -> qUser.userId.eq(filter.getUserId())).build();

			/**
			 * 
			 */
			if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
				BooleanExpression exp = qUser.roleCodes.contains(filter.getRoles().get(0).name());
				for (int i = 1; i < filter.getRoles().size(); i++) {
					exp = exp.or(qUser.roleCodes.contains(filter.getRoles().get(i).name()));
				}
				query = query.and(exp);
			}

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
	public UserDTO getUser(String identifier, IdType type, boolean includeFullDetails) throws Exception {
		QUserProfileEntity qUser = QUserProfileEntity.userProfileEntity;
		BooleanBuilder query = WhereClause.builder().optionalAnd(type == IdType.EMAIL, () -> qUser.email.eq(identifier))
				.optionalAnd(type == IdType.ID, () -> qUser.id.eq(identifier))
				.optionalAnd(type == IdType.AUTH_USER_ID, () -> qUser.userId.eq(identifier)).build();
		List<UserProfileEntity> result = new ArrayList<>();
		profileRepository.findAll(query).iterator().forEachRemaining(result::add);
		UserProfileEntity profile = result.stream().findFirst().orElseThrow();
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
		newAuth0User.setFullName(userDTO.getFirstName() + " " + userDTO.getLastName());

		newAuth0User.setPassword(userDTO.getPassword());
		newAuth0User.setEmailVerified(
				userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().getEmailVerified() : null);
		newAuth0User.setProviders(userDTO.getLoginProviders());
		
		if (userDTO.getUserId() != null) {
			newAuth0User.setUserId(userDTO.getUserId());
		}else {
			newAuth0User = authManagementService.createUser(newAuth0User);
		}

		/**
		 * Checking if user exists with the email if not available Creating user profile
		 * in local DB if available updating with latest details available System will
		 * throw error if creation is happening with existing records
		 * 
		 */
		Optional<UserProfileEntity> userProfile = profileRepository.findByEmail(userDTO.getEmail());
		UserProfileEntity profile = userProfile.isPresent() ? userProfile.get() : new UserProfileEntity();
		if (!userProfile.isPresent()) {
			profile.setId(UUID.randomUUID().toString());
			userDTO.setStatus(ProfileStatus.ACTIVE);
		}
		profile.setTitle(userDTO.getTitle());
		profile.setFirstName(newAuth0User.getFirstName());
		profile.setMiddleName(userDTO.getMiddleName());
		profile.setLastName(newAuth0User.getLastName());
		profile.setAvatarUrl(newAuth0User.getPicture() != null ? newAuth0User.getPicture() : userDTO.getImageUrl());
		profile.setDateOfBirth(userDTO.getDateOfBirth());
		profile.setGender(userDTO.getGender());
		profile.setEmail(newAuth0User.getEmail());
		profile.setAbout(userDTO.getAbout());
		profile.setCreatedBy(null);
		profile.setActiveContributor(
				userDTO.getAdditionalDetails() == null ? null : userDTO.getAdditionalDetails().isActiveContributor());
		profile.setCreatedOn(newAuth0User.getCreatedAt() != null ? newAuth0User.getCreatedAt() : CommonUtils.getSystemDate());
		profile.setDeleted(false);
		profile.setPublicProfile(
				userDTO.getAdditionalDetails() == null ? null : userDTO.getAdditionalDetails().isDisplayPublic());
		profile.setStatus(userDTO.getStatus() == null ? null : userDTO.getStatus().name());
		profile.setUserId(newAuth0User.getUserId());

		fillAddressMobileSocialMedia(profile, userDTO);

		profile = profileRepository.save(profile);
		/*
		 * Updating user metatdata
		 */
		AuthUser authuser = new AuthUser();
		authuser.setInactive(false);
		authuser.setProfileId(profile.getId());
		newAuth0User = authManagementService.updateUser(profile.getUserId(), authuser);
		return InfraDTOHelper.convertToUserDTO(profile, newAuth0User);
	}

	private void fillAddressMobileSocialMedia(UserProfileEntity profile, UserDTO userDTO) {
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
					profile.setPermanentAddressLine1(addressDTO.getAddressLine1());
					profile.setPermanentAddressLine2(addressDTO.getAddressLine2());
					profile.setPermanentAddressLine3(addressDTO.getAddressLine3());
					profile.setPermanentHometown(addressDTO.getHometown());
					profile.setPermanentDistrict(addressDTO.getDistrict());
					profile.setPermanentState(addressDTO.getState());
					profile.setPermanentCountry(addressDTO.getCountry());

					break;
				default:
				}
			}
			profile.setPresentPermanentSame(userDTO.getPresentPermanentSame());

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
	}

	@Override
	public UserDTO updateUser(String profileId, UserDTO userDTO) throws Exception {
		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		AuthUser updateAuthUser = new AuthUser();
		updateAuthUser.setFirstName(userDTO.getFirstName() == null ? profile.getFirstName() : userDTO.getFirstName());
		updateAuthUser.setLastName(userDTO.getLastName() == null ? profile.getLastName() : userDTO.getLastName());

		UserProfileEntity updatedProfile = new UserProfileEntity();
		updatedProfile.setTitle(userDTO.getTitle());
		if (userDTO.getFirstName() != null && userDTO.getLastName() != null) {
			updatedProfile.setFirstName(userDTO.getFirstName());
			updateAuthUser.setFirstName(userDTO.getFirstName());
			updatedProfile.setLastName(userDTO.getLastName());
			updateAuthUser.setLastName(userDTO.getLastName());
			updateAuthUser.setFullName(userDTO.getName() == null ? userDTO.getFirstName() + " " + profile.getLastName()
					: userDTO.getName());
		} else if (userDTO.getFirstName() != null) {
			updatedProfile.setFirstName(userDTO.getFirstName());
			updateAuthUser.setFirstName(userDTO.getFirstName());
			updateAuthUser.setFullName(userDTO.getName() == null ? userDTO.getFirstName() + " " + profile.getLastName()
					: userDTO.getName());
		} else if (userDTO.getLastName() != null) {
			updatedProfile.setLastName(userDTO.getLastName());
			updateAuthUser.setLastName(userDTO.getLastName());
			updateAuthUser.setFullName(userDTO.getName() == null ? userDTO.getFirstName() + " " + profile.getLastName()
					: userDTO.getName());
		}
		
		/**
		 * updating email and email verified
		 */
		boolean isEmailVerifiedUpdated = false;
		if (userDTO.getAdditionalDetails() != null && userDTO.getAdditionalDetails().getEmailVerified() != null) {
			updateAuthUser.setEmailVerified(
					userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().getEmailVerified() : null);
			isEmailVerifiedUpdated = true;
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
		
		fillAddressMobileSocialMedia(profile, userDTO);

		CommonUtils.copyNonNullProperties(updatedProfile, profile);

		/**
		 * If First name or last name is not null then considering there is some
		 * changes, or profile status to update
		 */
		if (userDTO.getFirstName() != null || userDTO.getLastName() != null || userDTO.getEmail() != null
				|| userDTO.getStatus() != null || isEmailVerifiedUpdated || userDTO.getProfileId() != null) {
			if (userDTO.getStatus() != null) {
				updateAuthUser.setInactive(ProfileStatus.ACTIVE != userDTO.getStatus());
				updateAuthUser.setBlocked(userDTO.getStatus() == ProfileStatus.BLOCKED);
			}
			updateAuthUser.setProfileId(profile.getId());
			updateAuthUser = authManagementService.updateUser(profile.getUserId(), updateAuthUser);
		}
		//boolean isPasswordUpdated = false;
		
		profile = profileRepository.save(profile);
		return InfraDTOHelper.convertToUserDTO(profile, updateAuthUser);
	}
	
	//changePassword(String )

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
	public List<RoleDTO> getUserRoles(String identifier, IdType type, boolean isActiveRole) throws Exception {
		List<AuthUserRole> roles = new ArrayList<>();
		if (isActiveRole && type == IdType.ID) {
			UserProfileEntity profile = profileRepository.findById(identifier).orElseThrow();
			roles.addAll(authManagementService.getRoles(profile.getUserId()));
		} else if (isActiveRole && type == IdType.EMAIL) {
			UserProfileEntity profile = profileRepository.findByEmail(identifier).orElseThrow();
			roles.addAll(authManagementService.getRoles(profile.getUserId()));
		} else if (isActiveRole && type == IdType.AUTH_USER_ID) {
			roles.addAll(authManagementService.getRoles(identifier));
		}
		return roles.stream().map(m -> InfraDTOHelper.convertToRoleDTO(m)).toList();
	}

	
	@Override
	public void updateUserRoles(String profileId, List<RoleDTO> roles) throws Exception {

		UserProfileEntity profile = profileRepository.findById(profileId).orElseThrow();
		List<String> currentRoles = InfraFieldHelper.stringToStringList(profile.getRoleCodes());
		List<String> newRoles = roles.stream().map(m -> m.getCode().name()).toList();
		Set<String> rolesToRemove=new HashSet<>(currentRoles);
		rolesToRemove.removeAll(new HashSet<>(newRoles));
		
		Set<String> rolesToAdd=new HashSet<>(newRoles);
		rolesToAdd.removeAll(new HashSet<>(currentRoles));
		
		log.debug("rolesToRemove = "+rolesToRemove+" rolesToAdd = "+rolesToAdd);
		
		if (!rolesToRemove.isEmpty()) {
			authManagementService.removeRolesFromUser(profile.getUserId(), rolesToRemove.stream().map(this::getRoleId).collect(Collectors.toList()));
		}
		
		if (!rolesToAdd.isEmpty()) {
			authManagementService.assignRolesToUser(profile.getUserId(), rolesToAdd.stream().map(this::getRoleId).collect(Collectors.toList()));
		}

		if (!roles.isEmpty()) {
			profile.setRoleNames(InfraFieldHelper.stringListToString(roles.stream().map(m -> m.getName()).toList()));
			profile.setRoleCodes(
					InfraFieldHelper.stringListToString(roles.stream().map(m -> m.getCode().name()).toList()));

		}

		profileRepository.save(profile);
	}

	//@CacheEvict(cacheNames = "auth0",allEntries = true)
	@Override
	public void assignUsersToRole(RoleDTO role, List<String> userIds) throws Exception {
		List<String> currentUsers =getUsersByRole(List.of(role.getCode())).stream().map(m->m.getUserId()).collect(Collectors.toList());
		List<String> newUsers = userIds;
		Set<String> usersToRemove=new HashSet<>(currentUsers);
		usersToRemove.removeAll(new HashSet<>(newUsers));
		
		Set<String> usersToAdd=new HashSet<>(newUsers);
		usersToAdd.removeAll(new HashSet<>(currentUsers));
		log.debug("currentUsers",currentUsers,"usersToRemove",usersToRemove+" usersToAdd = "+usersToAdd);

		
		for(String userId:usersToRemove) {
			Optional<UserProfileEntity> profileOpt = profileRepository.findByUserId(userId);
			if(profileOpt.isPresent()) {
				UserProfileEntity profile = profileOpt.get();
				authManagementService.removeRolesFromUser(userId, List.of(getRoleId(role.getCode().name())));
				Thread.sleep(100);
				if(profile.getRoleNames() != null && profile.getRoleCodes() != null) {
					profile.setRoleNames(InfraFieldHelper.removeItemFromString(profile.getRoleNames() ,role.getName()));
					profile.setRoleCodes(InfraFieldHelper.removeItemFromString(profile.getRoleCodes() ,role.getCode().name()));
					profileRepository.save(profile);
				}
			}
		}
		
		if(!usersToAdd.isEmpty()) {
			authManagementService.assignUsersToRole(getRoleId(role.getCode().name()), usersToAdd.stream().collect(Collectors.toList()));
			for(String userId:usersToAdd) {
				Optional<UserProfileEntity> profileOpt = profileRepository.findByUserId(userId);
				if(profileOpt.isPresent()) {
					UserProfileEntity profile = profileOpt.get();
					profile.setRoleNames(InfraFieldHelper.addItemToString(profile.getRoleNames() ,role.getName()));
					profile.setRoleCodes(InfraFieldHelper.addItemToString(profile.getRoleCodes() ,role.getCode().name()));
					profileRepository.save(profile);
				}
			}
		}
		
	}

	@Override
	public String getPaswordPolicy() throws Exception {
		AuthConnection connection = authManagementService.getConnections().stream()
				.filter(f -> f.isDatabaseConnection()).findFirst().get();
		return connection.getPasswordPolicy();
	}

	@Override
	public List<UserDTO> getUsersByRole(List<RoleCode> roles) throws Exception {
		List<UserDTO> finalUsers = new ArrayList<>();
		for (RoleCode role : roles) {
			List<UserDTO> Authusers = authManagementService.listUsersByRole(getRoleId(role.name())).stream()
					.map(m -> InfraDTOHelper.convertToUserDTO(null, m)).collect(Collectors.toList());
			finalUsers.addAll(Authusers);
		}
		return finalUsers.stream().distinct().collect(Collectors.toList());
	}

	@Override
	public List<UserDTO> getAuthUsers() throws Exception {
		return authManagementService.getUsers().stream()
				.map(authUser -> InfraDTOHelper.convertToUserDTO(null, authUser)).collect(Collectors.toList());
	}

	@Override
	public List<UserDTO> getUsersByRole(List<RoleCode> roles, Boolean isActive) throws Exception {
		List<UserDTO> finalUsers = new ArrayList<>();
		for (RoleCode role : roles) {
			Stream<AuthUser> stream = authManagementService.listUsersByRole(getRoleId(role.name())).stream();
			if (isActive != null) {
				stream = stream.filter(f -> !f.isInactive() == isActive);
			}
			List<UserDTO> authUsers = stream.map(m -> InfraDTOHelper.convertToUserDTO(null, m))
					.collect(Collectors.toList());
			finalUsers.addAll(authUsers);
		}
		return finalUsers.stream().distinct().collect(Collectors.toList());
	}

}
