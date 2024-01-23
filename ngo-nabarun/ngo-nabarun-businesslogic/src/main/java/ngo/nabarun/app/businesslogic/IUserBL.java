package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.UserDetailUpdate;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetailFilter;

@Service
public interface IUserBL {
	UserDetail getAuthUserFullDetails() throws Exception;
	UserDetail updateAuthUserDetails(UserDetailUpdate updatedUserDetails) throws Exception;
	UserDetail getUserDetails(String id, IdType idType,boolean includeAuthDetails,boolean includeRole) throws Exception;
	void initiatePasswordChange(String appClientId) throws Exception;
	void initiateEmailChange(String email)throws Exception;
	Paginate<UserDetail> getAllUser(Integer page, Integer size, UserDetailFilter filter);
	void assignRolesToUser(String id,List<RoleCode> roleCodes) throws Exception;
	void allocateUsersToRole(String id,List<String> users);

}
