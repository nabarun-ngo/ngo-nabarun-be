package ngo.nabarun.app.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IAdminBL;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/admin")
@SecurityRequirement(name = "nabarun_auth")
public class AdminController {
	
	@Autowired
	private IAdminBL adminBL;
	
	@PostMapping("/generateApiKey")
	public ResponseEntity<SuccessResponse<Map<String,String>>> generateApiKey(@RequestBody List<String> scopes)
			throws Exception {
		return new SuccessResponse<Map<String,String>>().payload(adminBL.generateApiKey(scopes)).get(HttpStatus.OK);
	}

	@PostMapping("/sync")
	public ResponseEntity<SuccessResponse<Void>> sync(@RequestBody List<String> items)
			throws Exception {
		if(items.contains("SYNC_AUTH0_USERS")) {
			adminBL.syncUsers();
		}
		
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
}
