package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;

@Service
public interface IUserBL {
	Paginate<UserDetail> getAllUser(Integer page, Integer size, UserDetailFilter filter) throws Exception;
	UserDetail getAuthUserFullDetails() throws Exception;
	UserDetail updateAuthUserDetails(UserDetail updatedUserDetails, boolean updatePicture) throws Exception;
	UserDetail getUserDetails(String id, IdType idType,boolean includeAuthDetails,boolean includeRole) throws Exception;
	void allocateUsersToRole(RoleCode roleCode,List<UserDetail> users) throws Exception;
	UserDetail updateUserDetail(String id, UserDetail detail) throws Exception;


}
