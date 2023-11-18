package ngo.nabarun.app.businesslogic.implementation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import ngo.nabarun.app.businesslogic.businessobjects.UserDetailUpdate;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.Page;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetailFilter;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainRefUtil;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectToDTOConverter;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.helper.GenericMockDataHelper;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
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
	private GenericMockDataHelper mockHelper;
	
	@Autowired 
	private GenericPropertyHelper propertyHelper;
	
//	@Autowired 
//	private BusinessEmailHelper emailHelper;
	
	@Autowired 
	private IDomainRefConfigInfraService domainRefConfig;
	
	@Autowired
	private IDocumentInfraService documentInfraService;
	
	
	@Override
	public UserDetail getAuthUserFullDetails() throws Exception {
		UserDTO userDTO = null;
		List<RoleDTO> roleDTO = new ArrayList<>();
		if (SecurityUtils.isAuthenticated()) {
			userDTO = userService.getUserByUserId(propertyHelper.isTokenMockingEnabledForTest() ? mockHelper.getAuthUserId() : SecurityUtils.getAuthUserId() , true);
			roleDTO =userService.getUserRoles(userDTO.getProfileId());  
		} else {
			throw new BusinessException(BusinessExceptionMessage.USER_AUTH_NEEDED.getMessage());
		}
		return DTOToBusinessObjectConverter.toUserDetail(userDTO,roleDTO);
	}

	@Override
	public UserDetail updateAuthUserDetails(UserDetailUpdate updatedUserDetails) throws Exception {
		UserDTO userDTO = null;
		if (SecurityUtils.isAuthenticated()) {
			userDTO = userService.getUserByUserId(propertyHelper.isTokenMockingEnabledForTest() ? mockHelper.getAuthUserId() : SecurityUtils.getAuthUserId() , false);
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
			
			if(updatedUserDetails.getGender() !=null && updatedUserDetails.getTitle()!=null && !BusinessDomainRefUtil.isTitleGenderAligned(userConfig,updatedUserDetails.getTitle(), updatedUserDetails.getGender())) {
				throw new BusinessException("Title '"+BusinessDomainRefUtil.getTitleValue(userConfig, updatedUserDetails.getTitle())+"' is not aligned with gender '"+BusinessDomainRefUtil.getGenderValue(userConfig, updatedUserDetails.getGender())+"'.");
			}else {
				/**
				 * Allowing to update 'gender' if it is compatible with title
				 * Throwing error when if only gender is getting changed then call config and check if new gender is aligned with old title
				 */
				if(updatedUserDetails.getGender() !=null && !BusinessDomainRefUtil.isTitleGenderAligned(userConfig, userDTO.getTitle(), updatedUserDetails.getGender())) {
					throw new BusinessException("Title '"+BusinessDomainRefUtil.getTitleValue(userConfig, userDTO.getTitle())+"' is not aligned with gender '"+BusinessDomainRefUtil.getGenderValue(userConfig, updatedUserDetails.getGender())+"'.");
				}
				/**
				 * Allowing to update 'title' if it is compatible with gender
				 * Throwing error when if only title is getting changed then call config and check if new title is aligned with old gender
				 */
				if(updatedUserDetails.getTitle() !=null && !BusinessDomainRefUtil.isTitleGenderAligned(userConfig, updatedUserDetails.getTitle(), userDTO.getGender())) {
					throw new BusinessException("Title '"+BusinessDomainRefUtil.getTitleValue(userConfig, userDTO.getTitle())+"' is not aligned with gender '"+BusinessDomainRefUtil.getGenderValue(userConfig, updatedUserDetails.getGender())+"'.");
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
		} else {
			throw new BusinessException(BusinessExceptionMessage.USER_AUTH_NEEDED.getMessage());
		}
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
			userDTO = userService.getUserByProfileId(id, includeAuthDetails);
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
	public Page<UserDetail> getAllUser(Integer page, Integer size, UserDetailFilter userDetailFilter) {
		UserDTO userDTOFilter = null;
		if(userDetailFilter != null) {
			userDTOFilter= new UserDTO();
			userDTOFilter.setFirstName(userDetailFilter.getFirstName());
			userDTOFilter.setLastName(userDetailFilter.getLastName());
			userDTOFilter.setEmail(userDetailFilter.getEmail());
			userDTOFilter.setPrimaryPhoneNumber(userDetailFilter.getPhoneNumber());
			userDTOFilter.setUserIds(List.of(userDetailFilter.getUserId()));
		}
		List<UserDetail> content =userService.getUsers(page, size, userDTOFilter).stream()
				.map(m -> DTOToBusinessObjectConverter.toUserDetail(m)).sorted(new Comparator<UserDetail>() {
					@Override
					public int compare(UserDetail o1, UserDetail o2) {
						return o1.getFirstName().compareTo(o2.getFirstName());
					}
				}).toList();
		
		long total;
		if(page != null && size != null){
			total=userService.getUsersCount();
		}else {
			total = content.size();
		}
		return new Page<UserDetail>(page, size, total, content);
	}

	@Override
	public void initiatePasswordChange(String appClientId) throws Exception {
		if (SecurityUtils.isAuthenticated()) {
			//String ticket =userService.initiatePasswordReset(SecurityUtils.getAuthUserId(),appClientId,0);
			//emailHelper.sendEmailOnPasswordChangeRequest
		} else {
			throw new BusinessException(BusinessExceptionMessage.USER_AUTH_NEEDED.getMessage());
		}		
	}

	@Override
	public void initiateEmailChange(String newEmail) throws Exception {
		if (SecurityUtils.isAuthenticated()) {
			//UserDTO userDTO = userService.getUserByUserId(SecurityUtils.getAuthUserId(), false);
			//String ticket =userService.initiateEmailChange(userDTO.getProfileId(),newEmail);
			//send email from here
			SecurityUtils.getAuthUserName();
		} else {
			throw new BusinessException(BusinessExceptionMessage.USER_AUTH_NEEDED.getMessage());
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
				role.setName(roleConfig.getDisplayValue());
				rolesToBeAdded.add(role);
			}
		}
		userService.addRolesToUser(id, rolesToBeAdded);
	}

	@Override
	public void allocateUsersToRole(String roleId, List<String> users) {
		
	}
}
