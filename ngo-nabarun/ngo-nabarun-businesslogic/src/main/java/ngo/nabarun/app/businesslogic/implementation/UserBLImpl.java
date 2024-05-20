package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
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
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
public class UserBLImpl implements IUserBL {

	@Autowired
	private IUserInfraService userService;
	
	@Autowired 
	private GenericPropertyHelper propertyHelper;
	
	@Autowired
	private BusinessHelper businessHelper;
	
	@Autowired
	private IDocumentInfraService documentInfraService;
	

	
	@Override
	public UserDetail getAuthUserFullDetails() throws Exception {
		UserDTO userDTO = null;
		if (SecurityUtils.isAuthenticated()) {
			userDTO = userService.getUser(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId() : SecurityUtils.getAuthUserId() ,IdType.AUTH_USER_ID  , true);
			List<RoleDTO> roleDTO =userService.getUserRoles(userDTO.getProfileId(),IdType.ID,true);  
			userDTO.setRoles(roleDTO);
		} 
		return DTOToBusinessObjectConverter.toUserDetail(userDTO);
	}

	@Override
	public UserDetail updateAuthUserDetails(UserDetailUpdate updatedUserDetails) throws Exception {
		UserDTO userDTO = null;
		if (SecurityUtils.isAuthenticated()) {
			userDTO = userService.getUser(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId() : SecurityUtils.getAuthUserId() ,IdType.AUTH_USER_ID, false);
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
			
			if(updatedUserDetails.getGender() !=null && updatedUserDetails.getTitle()!=null && !businessHelper.isTitleGenderAligned(updatedUserDetails.getTitle(), updatedUserDetails.getGender())) {
				throw new BusinessException("Title '"+businessHelper.getTitleValue(updatedUserDetails.getTitle())+"' is not aligned with gender '"+businessHelper.getGenderValue(updatedUserDetails.getGender())+"'.");
			}else {
				/**
				 * Allowing to update 'gender' if it is compatible with title
				 * Throwing error when if only gender is getting changed then call config and check if new gender is aligned with old title
				 */
				if(updatedUserDetails.getGender() !=null && !businessHelper.isTitleGenderAligned(userDTO.getTitle(), updatedUserDetails.getGender())) {
					throw new BusinessException("Title '"+businessHelper.getTitleValue(userDTO.getTitle())+"' is not aligned with gender '"+businessHelper.getGenderValue(updatedUserDetails.getGender())+"'.");
				}
				/**
				 * Allowing to update 'title' if it is compatible with gender
				 * Throwing error when if only title is getting changed then call config and check if new title is aligned with old gender
				 */
				if(updatedUserDetails.getTitle() !=null && !businessHelper.isTitleGenderAligned(updatedUserDetails.getTitle(), userDTO.getGender())) {
					throw new BusinessException("Title '"+businessHelper.getTitleValue(userDTO.getTitle())+"' is not aligned with gender '"+businessHelper.getGenderValue(updatedUserDetails.getGender())+"'.");
				}
			}
			
			/** 
			 *  *******************************************
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
		return DTOToBusinessObjectConverter.toUserDetail(userDTO);

	}

	@Override
	public UserDetail getUserDetails(String id, IdType idType,boolean includeAuthDetails,boolean includeRole) throws Exception {
		UserDTO userDTO = userService.getUser(id,idType, includeAuthDetails);;
		if(includeRole) {
			List<RoleDTO> roleDTO =userService.getUserRoles(userDTO.getProfileId(),IdType.ID,true);  
			userDTO.setRoles(roleDTO);
		}
		return DTOToBusinessObjectConverter.toUserDetail(userDTO);
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
		return new Paginate<UserDetail>(content);
	}


	@Override
	public void assignRolesToUser(String id, List<RoleCode> roleCodes) throws Exception {
		/**
		 * Validations
		 */
		if(CollectionUtils.isEmpty(roleCodes)) {
			throw new Exception("Collection cannot be empty") ;
		}
		List<RoleDTO> roles=businessHelper.convertToRoleDTO(roleCodes);
		userService.updateUserRoles(id, roles);
	}

	@Override
	public void allocateUsersToRole(String roleId, List<String> users) {
		
	}

	@Override
	@Cacheable("public_profiles")
	public List<UserDetail> getPublicProfiles() {
		UserDTOFilter userDTOFilter = new UserDTOFilter();
		userDTOFilter.setPublicProfile(true);
		userDTOFilter.setDeleted(false);
		List<UserDTO> content =userService.getUsers(null, null, userDTOFilter).getContent();
		return content.stream().map(DTOToBusinessObjectConverter::toPublicUserDetail).toList();
		
	}
	
	@Override
	public void initiatePasswordChange(String appClientId) throws Exception {
		if (SecurityUtils.isAuthenticated()) {
			//String ticket =userService.initiatePasswordReset(SecurityUtils.getAuthUserId(),appClientId,0);
			//emailHelper.sendEmailOnPasswordChangeRequest
		} 	
	}

	@Override
	public void syncUserDetail() throws Exception {
		userService.syncUserDetails();
	}

	@Override
	public void initiateEmailChange(String email) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
