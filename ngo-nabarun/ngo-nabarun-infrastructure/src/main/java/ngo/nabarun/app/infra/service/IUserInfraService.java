package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.UserDTO.UserDTOFilter;

@Service
public interface IUserInfraService {
	Page<UserDTO> getUsers(Integer page,Integer size, UserDTOFilter filter);
	UserDTO getUser(String identifier,IdType type,boolean includeFullDetails) throws Exception;
	UserDTO createUser(UserDTO userDTO) throws Exception;
	UserDTO updateUser(String profileId, UserDTO userDTO) throws Exception;
	void deleteUser(String profileId) throws Exception;
	List<RoleDTO> getUserRoles(String identifier, IdType type, boolean isActiveRole) throws Exception;
	void updateUserRoles(String profileId, List<RoleDTO> roles) throws Exception;
	void assignUsersToRole(String roleId, List<UserDTO> users) throws Exception;
	String initiatePasswordReset(String userId, String appClientId, int expireInSec) throws Exception;
	String getPaswordPolicy() throws Exception;
	List<UserDTO> getUsersByRole(List<RoleCode> pendingWithRoles) throws Exception;
	void syncUserDetails() throws Exception;

}
