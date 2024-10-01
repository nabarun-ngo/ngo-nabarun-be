package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.domain.RequestDO;
import ngo.nabarun.app.businesslogic.domain.UserDO;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail.WorkDetailFilter;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;

@Service
public class UserBLImpl extends BaseBLImpl implements IUserBL {

	@Autowired
	private UserDO userDO;
	
	@Autowired
	private RequestDO requestDO;
	
	@Autowired
	private BusinessDomainHelper domainHelper;

	@Override
	public UserDetail getAuthUserFullDetails() throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		UserDTO user= userDO.retrieveUserDetail(userId, IdType.AUTH_USER_ID, true,true);
		return BusinessObjectConverter.toUserDetail(user,domainHelper.getDomainKeyValues());
	}

	@Override
	public UserDetail updateAuthUserDetails(UserDetail updatedUserDetails, boolean updatePicture) throws Exception {
		
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		UserDTO userDTO = userDO.retrieveUserDetail(userId, IdType.AUTH_USER_ID, false,false);
		/**
		 * Checking if title and gender is aligned or not Allowing to update 'gender'
		 * and 'title' if it is compatible Throwing error when if 'gender' and 'title'
		 * is getting changed then call config and check if new gender is aligned with
		 * new title
		 */
		if (updatedUserDetails.getGender() != null && updatedUserDetails.getTitle() != null) {
			businessHelper.throwBusinessExceptionIf(()->!businessHelper
					.isTitleGenderAligned(updatedUserDetails.getTitle(), updatedUserDetails.getGender()), ExceptionEvent.TITLE_GENDER_MISALIGNED);
		}
		/**
		 * Allowing to update 'gender' if it is compatible with title Throwing error
		 * when if only gender is getting changed then call config and check if new
		 * gender is aligned with old title
		 */
		else if (updatedUserDetails.getGender() != null) {
			businessHelper.throwBusinessExceptionIf(()->!businessHelper
					.isTitleGenderAligned(userDTO.getTitle(), updatedUserDetails.getGender()), ExceptionEvent.TITLE_GENDER_MISALIGNED);
		}
		/**
		 * Allowing to update 'title' if it is compatible with gender Throwing error
		 * when if only title is getting changed then call config and check if new title
		 * is aligned with old gender
		 */
		else if (updatedUserDetails.getTitle() != null) {
			businessHelper.throwBusinessExceptionIf(()->!businessHelper
					.isTitleGenderAligned(updatedUserDetails.getTitle(), userDTO.getGender()), ExceptionEvent.TITLE_GENDER_MISALIGNED);
		}
		
		
		/**
		 * Allow user profile update only if status is active Throwing error if user
		 * profile status is anything other than ACTIVE
		 */
		if (userDTO.getStatus() != ProfileStatus.ACTIVE) {
			throw new BusinessException(BusinessExceptionMessage.INVALID_STATE.getMessage());
		}
		
		UserDTO updatedUser=userDO.updateUserDetail(userDTO.getProfileId(), updatedUserDetails, updatePicture);
		WorkDetailFilter filter= new WorkDetailFilter();
		filter.setSourceType(RequestType.PROFILE_UPDATE_REQUEST);
		filter.setCompleted(false);
		List<WorkDTO> workItems=requestDO.retrieveUserWorkList(null, null, userId, filter).getContent();
		for(WorkDTO workItem:workItems) {
			WorkDetail workDetail=new WorkDetail();
			workDetail.setAdditionalFields(List.of(new AdditionalField(AdditionalFieldKey.remarks, "Profile details updated.",true)));
			requestDO.updateWorkItem(workItem, workDetail, userId, ((t, u) -> performWorkflowAction(t, u)));
		}
		return BusinessObjectConverter.toUserDetail(updatedUser,domainHelper.getDomainKeyValues());

	}

	@Override
	public UserDetail getUserDetails(String id, IdType idType, boolean includeAuthDetails, boolean includeRole)
			throws Exception {
		UserDTO user= userDO.retrieveUserDetail(id, idType, includeAuthDetails, includeRole);
		return BusinessObjectConverter.toUserDetail(user,domainHelper.getDomainKeyValues());

	}

	@Override
	public Paginate<UserDetail> getAllUser(Integer page, Integer size, UserDetailFilter userDetailFilter) throws Exception {
		return userDO.retrieveAllUsers(page, size, userDetailFilter).map(m->{
			try {
				return BusinessObjectConverter.toUserDetail(m,domainHelper.getDomainKeyValues());
			} catch (Exception e) {
				e.printStackTrace();
				return BusinessObjectConverter.toUserDetail(m,null);
			}
		});
	}

	@Override
	public void allocateUsersToRole(RoleCode roleCode, List<UserDetail> users) throws Exception {
		userDO.assignUsersToRole(roleCode, users.stream().map(m->m.getUserId()).collect(Collectors.toList()));
	}

	@Override
	public UserDetail updateUserDetail(String id, UserDetail detail) throws Exception {
		UserDTO userDTO=userDO.updateUserDetailAdmin(id, detail);
		return BusinessObjectConverter.toUserDetail(userDTO,null);
	}

}
