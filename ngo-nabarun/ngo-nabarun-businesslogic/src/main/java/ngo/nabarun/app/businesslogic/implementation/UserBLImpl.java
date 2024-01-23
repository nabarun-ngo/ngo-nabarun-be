package ngo.nabarun.app.businesslogic.implementation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import ngo.nabarun.app.businesslogic.businessobjects.UserDetailUpdate;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetailFilter;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainRefHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectToDTOConverter;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.UserDTO.UserDTOFilter;
import ngo.nabarun.app.infra.misc.KeyValuePair;
import ngo.nabarun.app.infra.misc.UserConfigTemplate;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IDomainRefConfigInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
public class UserBLImpl implements IUserBL {

	@Autowired
	private IUserInfraService userService;
	
	@Autowired 
	private GenericPropertyHelper propertyHelper;
	
	@Autowired 
	private IDomainRefConfigInfraService domainRefConfig;
	
	@Autowired
	private IDocumentInfraService documentInfraService;
	
	
	@Override
	public UserDetail getAuthUserFullDetails() throws Exception {
		UserDTO userDTO = null;
		List<RoleDTO> roleDTO = new ArrayList<>();
		if (SecurityUtils.isAuthenticated()) {
			userDTO = userService.getUserByUserId(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId() : SecurityUtils.getAuthUserId() , true);
			roleDTO =userService.getUserRoles(userDTO.getProfileId());  
		} 
//		else {
//			throw new BusinessException(BusinessExceptionMessage.USER_AUTH_NEEDED.getMessage());
//		}
		return DTOToBusinessObjectConverter.toUserDetail(userDTO,roleDTO);
	}

	@Override
	public UserDetail updateAuthUserDetails(UserDetailUpdate updatedUserDetails) throws Exception {
		UserDTO userDTO = null;
		if (SecurityUtils.isAuthenticated()) {
			userDTO = userService.getUserByUserId(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId() : SecurityUtils.getAuthUserId() , false);
			UserConfigTemplate userConfig=domainRefConfig.getUserConfig();
			/** *******************************************
			 *   All Business level validations start here
			 *  *******************************************
			 */
	
			/**
			 * Allow user profile update only if status is active
			 * Throwing error if user profile status is anything other than ACTIVE
			 */
			if(userDTO.getStatus() != ProfileStatus.ACTIVE) {
				throw new BusinessException(BusinessExceptionMessage.INVALID_STATE.getMessage());
			}
			
			/**
			 * Throwing error when 'addPicture' is true but 'base64Image' is null or empty
			 */
			if(updatedUserDetails.isAddPicture() && (updatedUserDetails.getBase64Image() == null || updatedUserDetails.getBase64Image().length()==0)) {
				throw new BusinessException(BusinessExceptionMessage.INVALID_DATA.getMessage(Map.of("FIELD_NAME","base64Image")));
			}
			
			/**
			 * Checking if title and gender is aligned or not
			 * Allowing to update 'gender' and 'title' if it is compatible
			 * Throwing error when if 'gender' and 'title' is getting changed then call config and check if new gender is aligned with new title 
			 */
			
			if(updatedUserDetails.getGender() !=null && updatedUserDetails.getTitle()!=null && !BusinessDomainRefHelper.isTitleGenderAligned(userConfig,updatedUserDetails.getTitle(), updatedUserDetails.getGender())) {
				throw new BusinessException("Title '"+BusinessDomainRefHelper.getTitleValue(userConfig, updatedUserDetails.getTitle())+"' is not aligned with gender '"+BusinessDomainRefHelper.getGenderValue(userConfig, updatedUserDetails.getGender())+"'.");
			}else {
				/**
				 * Allowing to update 'gender' if it is compatible with title
				 * Throwing error when if only gender is getting changed then call config and check if new gender is aligned with old title
				 */
				if(updatedUserDetails.getGender() !=null && !BusinessDomainRefHelper.isTitleGenderAligned(userConfig, userDTO.getTitle(), updatedUserDetails.getGender())) {
					throw new BusinessException("Title '"+BusinessDomainRefHelper.getTitleValue(userConfig, userDTO.getTitle())+"' is not aligned with gender '"+BusinessDomainRefHelper.getGenderValue(userConfig, updatedUserDetails.getGender())+"'.");
				}
				/**
				 * Allowing to update 'title' if it is compatible with gender
				 * Throwing error when if only title is getting changed then call config and check if new title is aligned with old gender
				 */
				if(updatedUserDetails.getTitle() !=null && !BusinessDomainRefHelper.isTitleGenderAligned(userConfig, updatedUserDetails.getTitle(), userDTO.getGender())) {
					throw new BusinessException("Title '"+BusinessDomainRefHelper.getTitleValue(userConfig, userDTO.getTitle())+"' is not aligned with gender '"+BusinessDomainRefHelper.getGenderValue(userConfig, updatedUserDetails.getGender())+"'.");
				}
			}
			
			/** *******************************************
			 *   All Business level validations end here
			 *  *******************************************
			 */
			UserDTO updatedUserDTO=BusinessObjectToDTOConverter.toUserDTO(updatedUserDetails);
			
			/**
			 * Updating profile picture
			 */
			if (updatedUserDetails.isRemovePicture()) {
				List<DocumentDTO> profilePics = documentInfraService.getDocumentList(userDTO.getProfileId(),
						DocumentIndexType.PROFILE_PHOTO);
				for (DocumentDTO doc : profilePics) {
					documentInfraService.hardDeleteDocument(doc.getDocId());
				}
				updatedUserDTO.setImageUrl("");
			} 
			if (updatedUserDetails.isAddPicture()) {
				List<DocumentDTO> profilePics = documentInfraService.getDocumentList(userDTO.getProfileId(),
						DocumentIndexType.PROFILE_PHOTO);
				for (DocumentDTO doc : profilePics) {
					documentInfraService.hardDeleteDocument(doc.getDocId());
				}
				byte[] content = Base64.decodeBase64(updatedUserDetails.getBase64Image());
				DocumentDTO doc = documentInfraService.uploadDocument("pp.png", "image/png", userDTO.getProfileId(),
						DocumentIndexType.PROFILE_PHOTO, content);
				updatedUserDTO.setImageUrl(doc.getDocumentURL());
			}
			userDTO = userService.updateUser(userDTO.getProfileId(),updatedUserDTO);
		} 
//		else {
//			throw new BusinessException(BusinessExceptionMessage.USER_AUTH_NEEDED.getMessage());
//		}
		return DTOToBusinessObjectConverter.toUserDetail(userDTO);

	}

	@Override
	public UserDetail getUserDetails(String id, IdType idType,boolean includeAuthDetails,boolean includeRole) throws Exception {
		UserDTO userDTO = null;
		switch (idType) {
		case EMAIL:
			userDTO = userService.getUserByEmail(id, includeAuthDetails);
			break;
		case ID:
			userDTO = userService.getUser(id, includeAuthDetails);
			break;
		case AUTH_USER_ID:
			userDTO = userService.getUserByUserId(id, includeAuthDetails);
			break;
		default:
			break;
		}
		List<RoleDTO> roleDTO = null;
		if(includeRole) {
			roleDTO =userService.getUserRoles(userDTO.getProfileId());  
		}
		return DTOToBusinessObjectConverter.toUserDetail(userDTO,roleDTO);
	}

	@Override
	public Paginate<UserDetail> getAllUser(Integer page, Integer size, UserDetailFilter userDetailFilter) {
		UserDTOFilter userDTOFilter = null;
		if(userDetailFilter != null) {
			userDTOFilter= new UserDTOFilter();
			userDTOFilter.setFirstName(userDetailFilter.getFirstName());
			userDTOFilter.setLastName(userDetailFilter.getLastName());
			userDTOFilter.setEmail(userDetailFilter.getEmail());
			userDTOFilter.setPhoneNumber(userDetailFilter.getPhoneNumber());
			userDTOFilter.setUserId(userDetailFilter.getUserId());
		}
		Page<UserDetail> content =userService.getUsers(page, size, userDTOFilter)
				
				.map(m -> DTOToBusinessObjectConverter.toUserDetail(m));
//				.sorted(new Comparator<UserDetail>() {
//					@Override
//					public int compare(UserDetail o1, UserDetail o2) {
//						return o1.getFirstName().compareTo(o2.getFirstName());
//					}
//				}).toList();

		return new Paginate<UserDetail>(content);
	}

	@Override
	public void initiatePasswordChange(String appClientId) throws Exception {
		if (SecurityUtils.isAuthenticated()) {
			//String ticket =userService.initiatePasswordReset(SecurityUtils.getAuthUserId(),appClientId,0);
			//emailHelper.sendEmailOnPasswordChangeRequest
		} 	
	}

	@Override
	public void initiateEmailChange(String newEmail) throws Exception {
		if (SecurityUtils.isAuthenticated()) {
			//UserDTO userDTO = userService.getUserByUserId(SecurityUtils.getAuthUserId(), false);
			//String ticket =userService.initiateEmailChange(userDTO.getProfileId(),newEmail);
			//send email from here
			SecurityUtils.getAuthUserName();
		}
		
	}

	@Override
	public void assignRolesToUser(String id, List<RoleCode> roleCodes) throws Exception {
		List<RoleDTO> roles=userService.getUserRoles(id);
		UserConfigTemplate userConfig =domainRefConfig.getUserConfig();
		/**
		 * Validations
		 */
		
		if(CollectionUtils.isEmpty(roleCodes)) {
			throw new BusinessException("Collection cannot be empty") ;
		}
		
		if(roleCodes.equals(roles.stream().map(m->m.getCode()).toList())) {
			throw new BusinessException("Past roles and new roles cannot be same.") ;
		}
		
		/**
		 * Removing all existing roles
		 */
		userService.deleteRolesFromUser(id, roles);
		
		List<RoleDTO> rolesToBeAdded =new ArrayList<>();
		for(KeyValuePair roleConfig:userConfig.getAvailableUserRoles()) {
			if(roleCodes.stream().anyMatch(m-> m.name().equalsIgnoreCase(roleConfig.getKey())) ) {
				RoleDTO role = new RoleDTO();
				role.setCode(RoleCode.valueOf(roleConfig.getKey()));
				role.setId(roleConfig.getAttributes().getOrDefault("ROLE_ID", "NO_VALUE").toString());
				role.setName(roleConfig.getValue());
				rolesToBeAdded.add(role);
			}
		}
		userService.addRolesToUser(id, rolesToBeAdded);
	}

	@Override
	public void allocateUsersToRole(String roleId, List<String> users) {
		
	}
}
