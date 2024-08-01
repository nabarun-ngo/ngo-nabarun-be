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
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.EmailOrPasswordUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetailFilter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,value = "/api/user")
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
	public ResponseEntity<SuccessResponse<UserDetail>> updateLoggedInUserDetails(
			@RequestBody UserDetail requestBody,
			@RequestParam(required = false) boolean updatePicture
			) throws Exception {
		return new SuccessResponse<UserDetail>().payload(userBL.updateAuthUserDetails(requestBody,updatePicture)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getUsers")
	public ResponseEntity<SuccessResponse<Paginate<UserDetail>>> getUsers(
			@RequestParam(required = false) Integer pageIndex,
			@RequestParam(required = false) Integer pageSize,
			UserDetailFilter filter
			) throws Exception {
		Paginate<UserDetail> userList=userBL.getAllUser(pageIndex,pageSize,filter);
		return new SuccessResponse<Paginate<UserDetail>>().payload(userList).get(HttpStatus.OK);
	}
	
	
	@GetMapping("/getUserDetails/{id}")
	public ResponseEntity<SuccessResponse<UserDetail>> getUserDetails(@PathVariable String id,@RequestParam(defaultValue = "ID") IdType idType) throws Exception {
		return new SuccessResponse<UserDetail>().payload(userBL.getUserDetails(id,idType,false,false)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getUserFullDetails/{id}")
	public ResponseEntity<SuccessResponse<UserDetail>> getUserFullDetails(@PathVariable String id,@RequestParam(defaultValue = "ID") IdType idType) throws Exception {
		return new SuccessResponse<UserDetail>().payload(userBL.getUserDetails(id,idType,true,true)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getUserRoleHistory/{id}")
	public ResponseEntity<SuccessResponse<String>> getUserRoleHistory(@PathVariable String id) {
		return new SuccessResponse<String>().payload("Hi").get(HttpStatus.OK);
	}
	
	
	@PostMapping("/assignRolesToUsers/{id}")
	public ResponseEntity<SuccessResponse<Void>> assignRolesToUsers(
			@PathVariable String id,
			@RequestBody List<RoleCode> roleCodes) throws Exception {
		userBL.assignRolesToUser(id, roleCodes);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}

	@PostMapping("/initiatePasswordChange")
	public ResponseEntity<SuccessResponse<Void>> initiatePasswordChange(@RequestBody EmailOrPasswordUpdate request) throws Exception {
		userBL.initiatePasswordChange(request.getAppClientId());
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	

	@PostMapping("/changeEmail")
	public ResponseEntity<SuccessResponse<Void>> changeEmail(@RequestBody EmailOrPasswordUpdate requestBody) throws Exception {
		userBL.initiateEmailChange(null);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@GetMapping("/sync")
	public ResponseEntity<SuccessResponse<Void>> sync() throws Exception {
		//userBL.syncUserDetail();
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
//	
//  @RequestMapping(method = RequestMethod.PATCH, value = UPDATE_MY_PROFILE)
//  public ResponseEntity<SuccessResponse<ProfileObject>> updateMyProfile(@RequestBody ProfileObject profileObject) {
//  	return new SuccessResponse<ProfileObject>().payload(profileService.updateMyProfile(profileObject))
//  			.message(contentStore.getRemoteMessage(FirebaseMessage.SUCCESS_MSG_ENTITY_UPDATED,Map.of("item","Your profile"))).get();
//  }
//	
//	
//
//	@Operation(summary = "", description = "User must have ")
//	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieved"), })
//	@RequestMapping(method = RequestMethod.GET, value = GET_ALL_PROFILES)
//	public ResponseEntity<SuccessResponse<List<ProfileSummaryObject>>> getAllProfiles() {
//		return new SuccessResponse<List<ProfileSummaryObject>>().payload(profileService.getAllProfileSummary()).get();
//	}
//
//	@Operation(summary = "", description = "User must have ",parameters =  {
//		@Parameter(in = ParameterIn.QUERY ,name = ID),
//		@Parameter(in = ParameterIn.QUERY ,name = TYPE,schema = @Schema(allowableValues = {
//				"userId",
//				"email",
//				"profileId"
//				}))
//	})
//	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieved"), })
//    @RequestMapping(method = RequestMethod.GET, value = GET_PROFILE_DETALS)
//    public ResponseEntity<SuccessResponse<ProfileObject>> getProfileDetails(
//    		@RequestParam(ID) String id,
//    		@RequestParam(TYPE) String type) {
//		if(type.equalsIgnoreCase("userId")) {
//			return new SuccessResponse<ProfileObject>().payload(profileService.getProfileUsingUserId(id)).get();
//		}
//		else if(type.equalsIgnoreCase("email")) {
//			return new SuccessResponse<ProfileObject>().payload(profileService.getProfileUsingEmail(id)).get();
//		}
//		else {
//			return new SuccessResponse<ProfileObject>().payload(profileService.getProfile(id)).get();
//		}
//    }

//    @RequestMapping(method = RequestMethod.PATCH, value = SPECIFIC_PROFILE)
//    public ResponseEntity<SuccessResponse<ProfileObject>> updateProfile(@PathVariable(PROFILE_ID) String id,@RequestBody ProfileSummaryObject profileObject) {
//    	ProfileObject profile=profileService.updateRolesAndAttributes(id,profileObject);
//    	return new SuccessResponse<ProfileObject>().payload(profile)
//    			.message(contentStore.getRemoteMessage(FirebaseMessage.SUCCESS_MSG_ENTITY_UPDATED,Map.of("item","Roles or Profile status"))).get();
//    }
//    
//    /**
//    @RequestMapping(method = RequestMethod.DELETE, value = UPDATE_OTHER_PROFILE)
//    public ResponseEntity<Object> deleteProfile(@PathVariable(PROFILE_ID) String id) {
//    	profileService.deleteMember(id);
//    	return ResponseBuilder.build().asSuccess()
//    			.message(messageStore.getRemoteMessage(MessageKey.SUCCESS_MSG_ENTITY_DELETED(),Map.of("item","Selected member"))).status(HttpStatus.OK);
//    }*/
//    
//    @RequestMapping(method = RequestMethod.POST, value = CHANGE_MY_PROFILE_PICTURE)
//    public ResponseEntity<SuccessResponse<Object>> uploadProfilePicture(@RequestParam(PROFILE_PICTURE) MultipartFile file) throws IOException {
//    	profileService.updateProfilePicture(file);
//    	return new SuccessResponse<>()
//    			.message(contentStore.getRemoteMessage(FirebaseMessage.SUCCESS_MSG_ENTITY_UPDATED,Map.of("item","Your Profile Picture"))).get();
//    }
//    
//    
//    @RequestMapping(method = RequestMethod.POST, value = REMOVE_MY_PROFILE_PICTURE)
//    public ResponseEntity<SuccessResponse<Object>> removeProfilePicture() {
//    	profileService.removeProfilePicture();
//    	return new SuccessResponse<>()
//    			.message(contentStore.getRemoteMessage(FirebaseMessage.SUCCESS_MSG_ENTITY_UPDATED,Map.of("item","Your Profile Picture"))).get();
//    }

}
