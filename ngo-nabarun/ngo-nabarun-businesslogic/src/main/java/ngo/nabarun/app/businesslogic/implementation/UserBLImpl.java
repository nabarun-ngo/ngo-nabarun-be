package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import ngo.nabarun.app.businesslogic.domain.UserDO;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetailFilter;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.util.SecurityUtils;

@Service
public class UserBLImpl extends BaseBLImpl implements IUserBL {

	@Autowired
	private UserDO userDO;

	@Override
	public UserDetail getAuthUserFullDetails() throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return userDO.retrieveUserDetail(userId, IdType.AUTH_USER_ID, true,true);
	}

	@Override
	public UserDetail updateAuthUserDetails(UserDetail updatedUserDetails, boolean updatePicture) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		UserDetail userDTO = userDO.retrieveUserDetail(userId, IdType.AUTH_USER_ID, false,false);
		/**
		 * Allow user profile update only if status is active Throwing error if user
		 * profile status is anything other than ACTIVE
		 */
		if (userDTO.getStatus() != ProfileStatus.ACTIVE) {
			throw new BusinessException(BusinessExceptionMessage.INVALID_STATE.getMessage());
		}

		/**
		 * Checking if title and gender is aligned or not Allowing to update 'gender'
		 * and 'title' if it is compatible Throwing error when if 'gender' and 'title'
		 * is getting changed then call config and check if new gender is aligned with
		 * new title
		 */

		if (updatedUserDetails.getGender() != null && updatedUserDetails.getTitle() != null && !businessHelper
				.isTitleGenderAligned(updatedUserDetails.getTitle(), updatedUserDetails.getGender())) {
			throw new BusinessException("Title '" + businessHelper.getTitleValue(updatedUserDetails.getTitle())
					+ "' is not aligned with gender '" + businessHelper.getGenderValue(updatedUserDetails.getGender())
					+ "'.");
		} else {
			/**
			 * Allowing to update 'gender' if it is compatible with title Throwing error
			 * when if only gender is getting changed then call config and check if new
			 * gender is aligned with old title
			 */
			if (updatedUserDetails.getGender() != null
					&& !businessHelper.isTitleGenderAligned(userDTO.getTitle(), updatedUserDetails.getGender())) {
				throw new BusinessException(
						"Title '" + businessHelper.getTitleValue(userDTO.getTitle()) + "' is not aligned with gender '"
								+ businessHelper.getGenderValue(updatedUserDetails.getGender()) + "'.");
			}
			/**
			 * Allowing to update 'title' if it is compatible with gender Throwing error
			 * when if only title is getting changed then call config and check if new title
			 * is aligned with old gender
			 */
			if (updatedUserDetails.getTitle() != null
					&& !businessHelper.isTitleGenderAligned(updatedUserDetails.getTitle(), userDTO.getGender())) {
				throw new BusinessException(
						"Title '" + businessHelper.getTitleValue(userDTO.getTitle()) + "' is not aligned with gender '"
								+ businessHelper.getGenderValue(updatedUserDetails.getGender()) + "'.");
			}
		}

		return userDO.updateUserDetail(userDTO.getId(), updatedUserDetails, updatePicture);

	}

	@Override
	public UserDetail getUserDetails(String id, IdType idType, boolean includeAuthDetails, boolean includeRole)
			throws Exception {
		return userDO.retrieveUserDetail(id, idType, includeAuthDetails, includeRole);
	}

	@Override
	public Paginate<UserDetail> getAllUser(Integer page, Integer size, UserDetailFilter userDetailFilter) {
		return userDO.retrieveAllUsers(page, size, userDetailFilter);
	}

	@Override
	public void assignRolesToUser(String id, List<RoleCode> roleCodes) throws Exception {
		/**
		 * Validations
		 */
		if (CollectionUtils.isEmpty(roleCodes)) {
			throw new Exception("Collection cannot be empty");
		}
		userDO.assignRolesToUser(id, roleCodes);
	}

	@Override
	public void allocateUsersToRole(String roleId, List<String> users) {

	}
	
	@Override
	public void initiatePasswordChange(String appClientId) throws Exception {
		
	}

	@Override
	public void initiateEmailChange(String email) throws Exception {

	}

}
