package ngo.nabarun.app.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.helper.Authority;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IAdminBL;
import ngo.nabarun.app.businesslogic.businessobjects.CronServiceDetail;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/admin")
@SecurityRequirement(name = "nabarun_auth")
public class AdminController {

	@Autowired
	private IAdminBL adminBL;

	@PreAuthorize(Authority.READ_ADMIN_SERVICE)
	@GetMapping(value = "/apikey/list",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Void>> getApiKeyList(@RequestBody List<String> names) throws Exception {
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@PreAuthorize(Authority.CREATE_APIKEY)
	@PostMapping("/apikey/generate")
	public ResponseEntity<SuccessResponse<Map<String, String>>> generateApiKey(@RequestBody List<String> scopes)
			throws Exception {
		return new SuccessResponse<Map<String, String>>().payload(adminBL.generateApiKey(scopes)).get(HttpStatus.OK);
	}
	
	@PreAuthorize(Authority.UPDATE_APIKEY)
	@PostMapping("/apikey/{id}/update")
	public ResponseEntity<SuccessResponse<Map<String, String>>> updateApiKey(
			@PathVariable String id,
			@RequestParam boolean revoke,
			@RequestBody List<String> scopes)
			throws Exception {
		return new SuccessResponse<Map<String, String>>().payload(adminBL.updateApiKey(id,scopes,revoke)).get(HttpStatus.OK);
	}

	@Operation(summary = "Triggers a cron event in background.", description = "<table><thead><tr><th>Trigger Name</th><th>Parameters</th></tr></thead><tbody><tr><td>SYNC_USER</td><td>sync_role - Y/N (Sync roles with auth0)<br>user_id - &lt;user id &gt; (Sync specific user by id)<br>user_email - &lt;user email&gt; (Sync specific user by email)</td></tr></tbody></table>")
	@SecurityRequirement(name = "nabarun_auth_apikey")
	@PostMapping(value = "/cron/trigger")
	public ResponseEntity<SuccessResponse<Void>> triggerCron(@RequestBody List<CronServiceDetail> triggerDetail)
			throws Exception {
		adminBL.cronTrigger(triggerDetail);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@Operation(summary = "Runs a admin service.", description = "<table><thead><tr><th>Trigger Name</th><th>Parameters</th></tr></thead><tbody><tr><td>SYNC_USER</td><td>sync_role - Y/N (Sync roles with auth0)<br>user_id - &lt;user id &gt; (Sync specific user by id)<br>user_email - &lt;user email&gt; (Sync specific user by email)</td></tr></tbody></table>")
	@PreAuthorize(Authority.CREATE_SERVICERUN)
	@PostMapping(value = "/service/run")
	public ResponseEntity<SuccessResponse<Void>> runService(@RequestBody CronServiceDetail triggerDetail)
			throws Exception {
		adminBL.adminServices(triggerDetail);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@PostMapping(value = "/clearcache",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Void>> clearCache(@RequestBody List<String> names) throws Exception {
		adminBL.clearSystemCache(names);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	
	
}
