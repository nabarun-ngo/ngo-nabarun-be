package ngo.nabarun.app.api.controller;

import java.util.List;
import java.util.UUID;

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
import ngo.nabarun.app.businesslogic.businessobjects.ApiKeyDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail.JobDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/admin")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class AdminController {

	@Autowired
	private IAdminBL adminBL;

	@Operation(summary = "Retrieve list of apikeys",description = "Authorities : "+Authority.READ_APIKEY)
	@PreAuthorize(Authority.READ_APIKEY)
	@GetMapping(value = "/apikey/list",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<List<ApiKeyDetail>>> getApiKeyList() throws Exception {
		return new SuccessResponse<List<ApiKeyDetail>>().payload(adminBL.getApiKeys()).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Retrieve scope of api key",description = "Authorities : "+Authority.READ_APIKEY)
	@PreAuthorize(Authority.READ_APIKEY)
	@GetMapping(value = "/apikey/scopes",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<List<KeyValue>>> getApiKeyScopes() throws Exception {
		return new SuccessResponse<List<KeyValue>>().payload(adminBL.getApiKeyScopes()).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Create new apikey",description = "Authorities : "+Authority.CREATE_APIKEY)
	@PreAuthorize(Authority.CREATE_APIKEY)
	@PostMapping("/apikey/generate")
	public ResponseEntity<SuccessResponse<ApiKeyDetail>> generateApiKey(@RequestBody ApiKeyDetail apiKeyDetail)
			throws Exception {
		return new SuccessResponse<ApiKeyDetail>().payload(adminBL.generateApiKey(apiKeyDetail)).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Update existing apikey",description = "Authorities : "+Authority.UPDATE_APIKEY)
	@PreAuthorize(Authority.UPDATE_APIKEY)
	@PostMapping("/apikey/{id}/update")
	public ResponseEntity<SuccessResponse<ApiKeyDetail>> updateApiKey(
			@PathVariable String id,
			@RequestParam(required = false) boolean revoke,
			@RequestBody ApiKeyDetail apiKeyDetail)
			throws Exception {
		return new SuccessResponse<ApiKeyDetail>().payload(adminBL.updateApiKey(id,apiKeyDetail,revoke)).get(HttpStatus.OK);
	}

	
	@Operation(summary = "Runs a admin service.", description = "Authorities : "+Authority.CREATE_SERVICERUN
			+ "<br><br><table><thead><tr><th>Trigger Name</th><th>Parameters</th></tr></thead><tbody><tr><td>SYNC_USER</td><td>sync_role - Y/N (Sync roles with auth0)<br>user_id - &lt;user id &gt; (Sync specific user by id)<br>user_email - &lt;user email&gt; (Sync specific user by email)</td></tr></tbody></table>")
	@PreAuthorize(Authority.CREATE_SERVICERUN)
	@PostMapping(value = "/service/run")
	public ResponseEntity<SuccessResponse<Void>> runService(@RequestBody ServiceDetail triggerDetail)
			throws Exception {
		adminBL.adminServices(triggerDetail);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@Operation(summary = "Clears System cache")
	@PostMapping(value = "/clearcache",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Void>> clearCache(@RequestBody List<String> names) throws Exception {
		adminBL.clearSystemCache(names);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	
	@Operation(summary = "Retrieve job history",description = "Authorities : "+Authority.READ_JOB)
	@PreAuthorize(Authority.READ_JOB)
	@GetMapping(value = "/jobs/list",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Paginate<JobDetail>>> retrieveJobHistory(
			@RequestParam(required = false) Integer pageIndex, 
			@RequestParam(required = false) Integer pageSize,
			JobDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<JobDetail>>().payload(adminBL.getJobList(pageIndex,pageSize,filter)).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Triggers a job from external systems")
	@SecurityRequirement(name = "nabarun_auth_apikey")
	@PostMapping(value = "/jobs/trigger")
	public ResponseEntity<SuccessResponse<String>> jobsTrigger(@RequestBody List<ServiceDetail> serviceDetail)
			throws Exception {
		String triggerId=UUID.randomUUID().toString();
		adminBL.triggerJob(triggerId,serviceDetail);
		return new SuccessResponse<String>().payload(triggerId).get(HttpStatus.OK);
	}
}
