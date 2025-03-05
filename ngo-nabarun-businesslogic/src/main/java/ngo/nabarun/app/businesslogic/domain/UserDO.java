package ngo.nabarun.app.businesslogic.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserRole;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AdditionalConfigKey;
import ngo.nabarun.app.common.enums.AddressType;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.LoginMethod;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.UserDTO.UserDTOFilter;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Component
public class UserDO extends CommonDO {

	@Autowired
	private IUserInfraService userInfraService;

	@Autowired
	private IDocumentInfraService documentInfraService;

	private static String passwordPolicy;

	/**
	 * 
	 * @param page
	 * @param size
	 * @param userDetailFilter
	 * @return
	 * @throws Exception
	 */
	public Paginate<UserDTO> retrieveAllUsers(Integer page, Integer size, UserDetailFilter userDetailFilter)
			throws Exception {
		UserDTOFilter userDTOFilter = null;
		if (userDetailFilter != null) {
			userDTOFilter = new UserDTOFilter();
			userDTOFilter.setFirstName(userDetailFilter.getFirstName());
			userDTOFilter.setLastName(userDetailFilter.getLastName());
			userDTOFilter.setEmail(userDetailFilter.getEmail());
			userDTOFilter.setPhoneNumber(userDetailFilter.getPhoneNumber());
			userDTOFilter.setUserId(userDetailFilter.getUserId());
			userDTOFilter.setPublicProfile(userDetailFilter.getPublicFlag());
			userDTOFilter.setDeleted(false);
			userDTOFilter.setStatus(userDetailFilter.getStatus());
			userDTOFilter.setRoles(userDetailFilter.getRoles());
		}
		if (userDetailFilter.isUserByRole()) {
			List<UserDTO> users = userInfraService.getUsersByRole(userDetailFilter.getRoles());
			return new Paginate<UserDTO>(page, size, users.size(), users);
		}
		Page<UserDTO> content = userInfraService.getUsers(page, size, userDTOFilter);
		return new Paginate<UserDTO>(content);
	}

	/**
	 * 
	 * @param id
	 * @param idType
	 * @param fullDetail
	 * @return
	 * @throws Exception
	 */
	public UserDTO retrieveUserDetail(String id, IdType idType, boolean fullDetail) throws Exception {
		UserDTO userDTO = userInfraService.getUser(id, idType, fullDetail);
//		if (includeRole) {
//			List<RoleDTO> roleDTO = userInfraService.getUserRoles(id, idType, true);
//			userDTO.setRoles(roleDTO);
//		}
		return userDTO;
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	public boolean isUserExists(String email) {
		UserDTOFilter filter = new UserDTOFilter();
		filter.setEmail(email);
		int userCount = userInfraService.getUsers(null, null, filter).getSize();
		return userCount > 0;
	}

	/**
	 * 
	 * @param userdetail
	 * @param phoneCode
	 * @param phoneNumber
	 * @param hometown
	 * @param password
	 * @param emailVerified
	 * @param resetPassword
	 * @return
	 * @throws Exception
	 */
	public UserDTO createUser(String firstName, String lastName, String email, String phoneCode, String phoneNumber,
			String hometown, String password, boolean emailVerified, boolean resetPassword) throws Exception {
		UserDTO userDTO = new UserDTO();
		PhoneDTO phoneDto = new PhoneDTO();
		AddressDTO addressDto = new AddressDTO();

		UserAdditionalDetailsDTO additionalDetailDto = new UserAdditionalDetailsDTO();
		userDTO.setFirstName(firstName);
		userDTO.setLastName(lastName);
		userDTO.setEmail(email);

		phoneDto.setPhoneCode(phoneCode);
		phoneDto.setPhoneNumber(phoneNumber);
		phoneDto.setPhoneType(PhoneType.PRIMARY);
		addressDto.setHometown(hometown);
		addressDto.setAddressType(AddressType.PRESENT);
		userDTO.setPassword(password);

		additionalDetailDto.setActiveContributor(true);
		additionalDetailDto.setBlocked(false);
		additionalDetailDto.setDisplayPublic(false);
		additionalDetailDto.setEmailVerified(emailVerified);
		additionalDetailDto.setPasswordResetRequired(resetPassword);
		userDTO.setStatus(ProfileStatus.ACTIVE);
		userDTO.setPhoneNumber(phoneDto.getPhoneCode() + phoneDto.getPhoneNumber());
		userDTO.setPhones(List.of(phoneDto));
		userDTO.setAddresses(List.of(addressDto));
		userDTO.setAdditionalDetails(additionalDetailDto);
		List<LoginMethod> loginMethods = businessDomainHelper.getAvailableLoginMethods();
		String defaultRoleCode = businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.DEFAULT_ROLE_CODE);
		userDTO.setLoginProviders(loginMethods);
		userDTO = userInfraService.createUser(userDTO, true);
		List<RoleDTO> roles = businessDomainHelper.convertToRoleDTO(List.of(RoleCode.valueOf(defaultRoleCode)));
		userInfraService.updateUserRoles(userDTO.getProfileId(), roles);
		return userDTO;
	}

	/**
	 * 
	 * @param id
	 * @param updatedUserDetails
	 * @param updateProfilePic
	 * @return
	 * @throws Exception
	 */
	public UserDTO updateUserDetail(String id, UserDetail updatedUserDetails, boolean updateProfilePic)
			throws Exception {
		UserDTO updatedUserDTO = new UserDTO();
		updatedUserDTO.setTitle(updatedUserDetails.getTitle());
		updatedUserDTO.setFirstName(updatedUserDetails.getFirstName());
		updatedUserDTO.setMiddleName(updatedUserDetails.getMiddleName());
		updatedUserDTO.setLastName(updatedUserDetails.getLastName());
		updatedUserDTO.setGender(updatedUserDetails.getGender());
		updatedUserDTO.setDateOfBirth(updatedUserDetails.getDateOfBirth());
		updatedUserDTO.setAbout(updatedUserDetails.getAbout());
		updatedUserDTO.setGender(updatedUserDetails.getGender());
		updatedUserDTO.setEmail(updatedUserDetails.getEmail());/*****/

		updatedUserDTO.setAddresses(updatedUserDetails.getAddresses() == null ? List.of()
				: updatedUserDetails.getAddresses().stream().map(BusinessObjectConverter::toAddressDTO)
						.collect(Collectors.toList()));
		updatedUserDTO.setPresentPermanentSame(updatedUserDetails.getPresentAndPermanentAddressSame());
		updatedUserDTO.setPhones(BusinessObjectConverter.toPhoneDTO(updatedUserDetails.getPhoneNumbers()));
		updatedUserDTO
				.setSocialMedias(BusinessObjectConverter.toSocialMediaDTO(updatedUserDetails.getSocialMediaLinks()));

//		if (updatedUserDetails.getLoginMethod() != null) {
//			updatedUserDTO.setLoginProviders(updatedUserDetails.getLoginMethod());
//		}
		Map<String, Object> userAttr = updatedUserDetails.getAttributes();
		if (userAttr != null && userAttr.get("new_password") != null && userAttr.get("old_password") != null) {
			String old_password = new String(
					java.util.Base64.getDecoder().decode(userAttr.get("old_password").toString()));
			String new_password = new String(
					java.util.Base64.getDecoder().decode(userAttr.get("new_password").toString()));
			try {
				userInfraService.validatePassword(id, old_password);
			} catch (Exception e) {
				throw new BusinessException("Current Password is incorrect.", e);
			}
			updatedUserDTO.setPassword(new_password);
		}
		/**
		 * Updating profile picture
		 */

		UserDTO userDTO;
		if (updateProfilePic) {
			List<DocumentDTO> profilePics = documentInfraService.getDocumentList(id, DocumentIndexType.PROFILE_PHOTO);
			for (DocumentDTO doc : profilePics) {
				documentInfraService.hardDeleteDocument(doc.getDocId());
			}
			if (updatedUserDetails.getPictureBase64() != null) {
				byte[] content = Base64.decodeBase64(updatedUserDetails.getPictureBase64());
				DocumentDTO doc = documentInfraService.uploadDocument(UUID.randomUUID().toString() + ".png",
						"image/png", id, DocumentIndexType.PROFILE_PHOTO, content);
				updatedUserDTO.setImageUrl(doc.getDocumentURL());
			} else {
				updatedUserDTO.setImageUrl("");
			}
		}
		userDTO = userInfraService.updateUser(id, updatedUserDTO);
		return userDTO;
	}

	/**
	 * Updating admin attributes
	 */
	public UserDTO updateUserDetailAdmin(String id, UserDetail updatedUserDetails) throws Exception {
		UserDTO updatedUserDTO = new UserDTO();
		updatedUserDTO.setStatus(updatedUserDetails.getStatus());

		List<UserRole> rolesToUpdate = updatedUserDetails.getRoles();
		if (rolesToUpdate != null && !rolesToUpdate.isEmpty()) {
			List<RoleDTO> roles = businessDomainHelper
					.convertToRoleDTO(rolesToUpdate.stream().map(m -> m.getRoleCode()).collect(Collectors.toList()));
			userInfraService.updateUserRoles(id, roles);
		}

		UserDTO userDTO = userInfraService.updateUser(id, updatedUserDTO);
		if (updatedUserDetails.getLoginMethod() != null && !updatedUserDetails.getLoginMethod().isEmpty()) {
			userDTO.setLoginProviders(updatedUserDetails.getLoginMethod());
			userInfraService.createUser(userDTO, true);
		}
		return userDTO;
	}

	public UserDTO updateUserDetailAdmin(String id, UserDTO userDTO) throws Exception {
		List<RoleDTO> rolesToUpdate = userDTO.getRoles();
		if (rolesToUpdate != null && !rolesToUpdate.isEmpty()) {
			List<RoleDTO> roles = businessDomainHelper
					.convertToRoleDTO(rolesToUpdate.stream().map(m -> m.getCode()).collect(Collectors.toList()));
			userInfraService.updateUserRoles(id, roles);
		}
		userDTO = userInfraService.updateUser(id, userDTO);
		return userDTO;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getPasswordPolicy() throws Exception {
		if (passwordPolicy == null) {
			passwordPolicy = userInfraService.getPaswordPolicy();
		}
		return passwordPolicy;
	}

	public void assignUsersToRole(RoleCode roleCode, List<String> usersIds) throws Exception {
		RoleDTO roleDTO = businessDomainHelper.convertToRoleDTO(roleCode);
		userInfraService.assignUsersToRole(roleDTO, usersIds);
	}

	/**
	 * 
	 * @param job
	 * @param user_email
	 * @param user_id
	 * @throws Exception
	 */
	public void syncUserDetail(JobDTO job, boolean syncRole, String user_id, String user_email) throws Exception {
		job.log("[INFO] Starting User Detail Sync Job with SyncRole=" + syncRole + " UserId=" + user_id + " UserEmail="
				+ user_email);

		job.log("[INFO] Retrieveing user list from auth0...");
		UserDTOFilter filterDTO = new UserDTOFilter();
		filterDTO.setUserId(user_id);
		filterDTO.setEmail(user_email);
		List<UserDTO> usersDTOs = userInfraService.getAuthUsers(filterDTO);
		job.log("[INFO] " + usersDTOs.size() + " users retrieved from auth0.");
		for (UserDTO userDTO : usersDTOs) {
			job.log("[INFO] Trying to sync " + userDTO.getName() + " (" + userDTO.getUserId() + ").");
			try {
				job.log("[INFO] Trying to get " + userDTO.getEmail() + " from DB.");
				UserDTOFilter filter = new UserDTOFilter();
				filter.setEmail(userDTO.getEmail());
				List<UserDTO> users = userInfraService.getUsers(null, null, filter).getContent();
				job.log("[INFO] Found " + users.size() + " using email " + userDTO.getEmail() + " from DB.");
				if (!users.isEmpty()) {
					UserDTO firstUser = users.get(0);

					// Updating attributes
					UserDTO userDTO_U = new UserDTO();
					if (syncRole) {
						job.log("[INFO] Syncing roles from auth0 to DB");
						List<RoleDTO> roles = userInfraService.getUserRoles(firstUser.getUserId(), IdType.AUTH_USER_ID,
								true);
						List<RoleDTO> roleDTO = businessDomainHelper
								.convertToRoleDTO(roles.stream().map(m -> m.getCode()).collect(Collectors.toList()));
						userDTO_U.setRoles(roleDTO);
						job.log("[INFO] Role sync complete. On update this will reflect in DB");
					}
					userDTO_U.setFirstName(
							userDTO.getFirstName() == null ? firstUser.getFirstName() : userDTO.getFirstName());
					userDTO_U.setLastName(
							userDTO.getLastName() == null ? firstUser.getLastName() : userDTO.getLastName());
					userDTO_U.setName(userDTO.getName());
					;
					userDTO_U.setImageUrl(
							firstUser.getImageUrl() == null ? userDTO.getImageUrl() : firstUser.getImageUrl());

					job.log("[INFO] Syncing/Updating details from Auth0 to DB. ==> " + userDTO_U);
					userDTO_U = userInfraService.updateUser(users.get(0).getProfileId(), userDTO_U);
					job.log("[INFO] Details updated successfully to DB.");
					
					job.log("[INFO] Auth0 Profile Id ==> " + userDTO_U.getProfileId_Auth0());
				} else {
					job.log("[INFO] No user Found using email " + userDTO.getEmail()
							+ " from DB. Creating New User in DB.");
					userDTO.getAdditionalDetails().setActiveContributor(true);
					userInfraService.createUser(userDTO, false);
					job.log("[INFO] User creation completed.");
				}
				Thread.sleep(2000);
			} catch (Exception e) {
				job.log("[ERROR] Something went wrong while running job for " + userDTO.getEmail() + ". Message ==> "
						+ e.getMessage());
				e.printStackTrace();
			}

			// Thread.sleep(2000);
		}
	}

	public List<UserDTO> getUsers(List<RoleCode> codes) throws Exception {
		List<UserDTO> users = userInfraService.getUsersByRole(codes);
		return users;
	}

	public void deleteMember(String id) throws Exception {
		userInfraService.deleteUser(id);
	}

}
