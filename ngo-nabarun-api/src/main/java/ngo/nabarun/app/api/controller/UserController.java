package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.helper.Authority;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/user")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class UserController {

	@Autowired
	private IUserBL userBL;

	@GetMapping("/getLoggedInUserDetails")
	public ResponseEntity<SuccessResponse<UserDetail>> getLoggedInUserDetails() throws Exception {
		return new SuccessResponse<UserDetail>().payload(userBL.getAuthUserFullDetails()).get(HttpStatus.OK);
	}

	@PatchMapping("/updateLoggedInUserDetails")
	public ResponseEntity<SuccessResponse<UserDetail>> updateLoggedInUserDetails(@RequestBody UserDetail requestBody,
			@RequestParam(required = false) boolean updatePicture) throws Exception {
		return new SuccessResponse<UserDetail>().payload(userBL.updateAuthUserDetails(requestBody, updatePicture))
				.get(HttpStatus.OK);
	}

	@GetMapping("/getUsers")
	@PreAuthorize(Authority.READ_USERS)
	public ResponseEntity<SuccessResponse<Paginate<UserDetail>>> getUsers(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			UserDetailFilter filter) throws Exception {
		Paginate<UserDetail> userList = userBL.getAllUser(pageIndex, pageSize, filter);
		return new SuccessResponse<Paginate<UserDetail>>().payload(userList).get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.READ_USER)
	@GetMapping("/getUserDetails/{id}")
	public ResponseEntity<SuccessResponse<UserDetail>> getUserDetails(@PathVariable String id,
			@RequestParam(defaultValue = "ID") IdType idType) throws Exception {
		return new SuccessResponse<UserDetail>().payload(userBL.getUserDetails(id, idType, false, false))
				.get(HttpStatus.OK);
	}

//	@PreAuthorize(Authority.READ_PROFILE)
//	@GetMapping("/getUserFullDetails/{id}")
//	public ResponseEntity<SuccessResponse<UserDetail>> getUserFullDetails(@PathVariable String id,@RequestParam(defaultValue = "ID") IdType idType) throws Exception {
//		return new SuccessResponse<UserDetail>().payload(userBL.getUserDetails(id,idType,true,true)).get(HttpStatus.OK);
//	}

	@PreAuthorize(Authority.READ_USER)
	@GetMapping("/getUserRoleHistory/{id}")
	public ResponseEntity<SuccessResponse<String>> getUserRoleHistory(@PathVariable String id) {
		return new SuccessResponse<String>().payload("Hi").get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.UPDATE_USER)
	@PostMapping("/updateUserDetails/{id}")
	public ResponseEntity<SuccessResponse<UserDetail>> updateUserDetails(@PathVariable String id,
			@RequestBody UserDetail detail) throws Exception {
		UserDetail user = userBL.updateUserDetail(id, detail);
		return new SuccessResponse<UserDetail>().payload(user).get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.UPDATE_USER)
	@PostMapping("/assignUsersToRoles/{id}")
	public ResponseEntity<SuccessResponse<Void>> assignUsersToRoles(@PathVariable RoleCode id,
			@RequestBody List<UserDetail> users) throws Exception {
		userBL.allocateUsersToRole(id, users);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}

}
