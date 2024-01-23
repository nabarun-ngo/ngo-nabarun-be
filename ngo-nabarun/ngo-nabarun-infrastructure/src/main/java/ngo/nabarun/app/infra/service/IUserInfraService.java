package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.UserDTO.UserDTOFilter;

@Service
public interface IUserInfraService {
	Page<UserDTO> getUsers(Integer page,Integer size, UserDTOFilter filter);
	UserDTO getUser(String id,boolean includeFullDetails) throws Exception;
	UserDTO getUserByUserId(String userId,boolean includeFullDetails) throws Exception;
	UserDTO getUserByEmail(String email,boolean includeFullDetails) throws Exception;
		
	UserDTO createUser(UserDTO userDTO) throws Exception;
	UserDTO updateUser(String profileId, UserDTO userDTO) throws Exception;
	void deleteUser(String profileId) throws Exception;

	String initiatePasswordReset(String userId, String appClientId, int expireInSec) throws Exception;
	long getUsersCount();
	
	List<RoleDTO> getUserRoles(String profileId) throws Exception;
	void deleteRolesFromUser(String profileId, List<RoleDTO> roles) throws Exception;
	void addRolesToUser(String profileId, List<RoleDTO> roles) throws Exception;
	void assignUsersToRole(String roleId, List<UserDTO> users) throws Exception;

}
