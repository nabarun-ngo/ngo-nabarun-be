package ngo.nabarun.app.businesslogic.domain;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetailFilter;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AdditionalConfigKey;
import ngo.nabarun.app.common.enums.AddressType;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.UserDTO.UserDTOFilter;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Component
public class UserDO extends CommonDO{
	
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
	 */
	public Paginate<UserDTO> retrieveAllUsers(Integer page, Integer size, UserDetailFilter userDetailFilter) {
		UserDTOFilter userDTOFilter = null;
		if(userDetailFilter != null) {
			userDTOFilter= new UserDTOFilter();
			userDTOFilter.setFirstName(userDetailFilter.getFirstName());
			userDTOFilter.setLastName(userDetailFilter.getLastName());
			userDTOFilter.setEmail(userDetailFilter.getEmail());
			userDTOFilter.setPhoneNumber(userDetailFilter.getPhoneNumber());
			userDTOFilter.setUserId(userDetailFilter.getUserId());
			userDTOFilter.setPublicProfile(userDetailFilter.getPublicFlag());
			userDTOFilter.setDeleted(false);
		}
		Page<UserDTO> content =userInfraService.getUsers(page, size, userDTOFilter);
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
	public UserDTO retrieveUserDetail(String id,IdType idType,boolean fullDetail,boolean includeRole) throws Exception {
		UserDTO userDTO = userInfraService.getUser(id ,idType, fullDetail);
		if(includeRole) {
			List<RoleDTO> roleDTO =userInfraService.getUserRoles(id,idType,true);  
			userDTO.setRoles(roleDTO);
		}
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
	 * @return
	 * @throws Exception
	 */
	public UserDTO createUser(String firstName,String lastName,String email, String phoneCode,String phoneNumber, String hometown, String password,boolean emailVerified) throws Exception {
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
		userDTO.setStatus(ProfileStatus.ACTIVE);
		userDTO.setPhoneNumber(phoneDto.getPhoneCode() + phoneDto.getPhoneNumber());
		userDTO.setPhones(List.of(phoneDto));
		userDTO.setAddresses(List.of(addressDto));
		userDTO.setAdditionalDetails(additionalDetailDto);
		String[] loginMethods = businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.LOGIN_METHODS).split(",");
		String defaultRoleCode = businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.DEFAULT_ROLE_CODE);
		userDTO.setLoginProviders(List.of(loginMethods));
		userDTO = userInfraService.createUser(userDTO);
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
	public UserDTO updateUserDetail(String id,UserDetail updatedUserDetails,boolean updateProfilePic) throws Exception {
		UserDTO updatedUserDTO = new UserDTO();
		updatedUserDTO.setTitle(updatedUserDetails.getTitle());
		updatedUserDTO.setFirstName(updatedUserDetails.getFirstName());
		updatedUserDTO.setMiddleName(updatedUserDetails.getMiddleName());
		updatedUserDTO.setLastName(updatedUserDetails.getLastName());
		updatedUserDTO.setGender(updatedUserDetails.getGender());
		updatedUserDTO.setDateOfBirth(updatedUserDetails.getDateOfBirth());
		updatedUserDTO.setAbout(updatedUserDetails.getAbout());

		updatedUserDTO.setAddresses(updatedUserDetails.getAddresses() == null ? List.of()
				: updatedUserDetails.getAddresses().stream().map(BusinessObjectConverter::toAddressDTO).collect(Collectors.toList()));
		updatedUserDTO.setPhones(BusinessObjectConverter.toPhoneDTO(updatedUserDetails.getPhoneNumbers()));
		updatedUserDTO.setSocialMedias(BusinessObjectConverter.toSocialMediaDTO(updatedUserDetails.getSocialMediaLinks()));
		
		/**
		 * Updating profile picture
		 */
		 
		UserDTO userDTO;
		if (updateProfilePic) {
			List<DocumentDTO> profilePics = documentInfraService.getDocumentList(id,
					DocumentIndexType.PROFILE_PHOTO);
			for (DocumentDTO doc : profilePics) {
				documentInfraService.hardDeleteDocument(doc.getDocId());
			}
			if(updatedUserDetails.getPictureBase64() != null) {
				byte[] content = Base64.decodeBase64(updatedUserDetails.getPictureBase64());
				DocumentDTO doc = documentInfraService.uploadDocument(UUID.randomUUID().toString()+".png", "image/png", id,
						DocumentIndexType.PROFILE_PHOTO, content);
				updatedUserDTO.setImageUrl(doc.getDocumentURL());
			}else {
				updatedUserDTO.setImageUrl("");
			}
		}
		userDTO = userInfraService.updateUser(id,updatedUserDTO);
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

	/**
	 * 
	 * @param id
	 * @param roleCodes
	 * @throws Exception
	 */
	public void assignRolesToUser(String id, List<RoleCode> roleCodes) throws Exception {
		List<RoleDTO> roles=businessDomainHelper.convertToRoleDTO(roleCodes);
		userInfraService.updateUserRoles(id, roles);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void syncUsers() throws Exception {
		userInfraService.auth0UserSync();
	}
	
	public List<UserDTO> getUsers(List<RoleCode> codes) throws Exception {
		List<UserDTO> users= userInfraService.getUsersByRole(codes);
		return users;
	}

}
