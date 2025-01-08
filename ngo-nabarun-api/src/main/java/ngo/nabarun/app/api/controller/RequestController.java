package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import ngo.nabarun.app.businesslogic.IRequestBL;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail.RequestDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail.WorkDetailFilter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/request")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class RequestController {

	@Autowired
	private IRequestBL requestBL;

	@Operation(summary = "Retrieve list of requests of logged in user")
	@GetMapping("/list/self")
	public ResponseEntity<SuccessResponse<Paginate<RequestDetail>>> getMyRequests(
			@RequestParam(required = false) Integer pageIndex, 
			@RequestParam(required = false) Integer pageSize,
			RequestDetailFilter filter) throws Exception {
		Paginate<RequestDetail> requestList=requestBL.getMyRequests(pageIndex, pageSize,filter);
		return new SuccessResponse<Paginate<RequestDetail>>()
				.payload(requestList).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Retrieves details of a specific request",description = "Authorities : "+Authority.READ_REQUEST)
	@PreAuthorize(Authority.READ_REQUEST)
	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<RequestDetail>> getRequestDetail(@PathVariable String id) throws Exception {
		return new SuccessResponse<RequestDetail>()
				.payload(requestBL.getRequest(id)).get(HttpStatus.OK); 
	}
	
	@Operation(summary = "Create a new request",description = "Authorities : "+Authority.CREATE_REQUEST)
	@PreAuthorize(Authority.CREATE_REQUEST)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<RequestDetail>> createRequest(@RequestBody RequestDetail createRequest) throws Exception {
		return new SuccessResponse<RequestDetail>()
				.payload(requestBL.createRequest(createRequest)).get(HttpStatus.OK); 
	}
	
	@Operation(summary = "Update an existing requests",description = "Authorities : "+Authority.UPDATE_REQUEST)
	@PreAuthorize(Authority.UPDATE_REQUEST)
	@PatchMapping("/{id}/update")
	public ResponseEntity<SuccessResponse<RequestDetail>> updateRequest(@PathVariable String id,@RequestBody RequestDetail request) throws Exception {
		RequestDetail worklist=requestBL.updateRequest(id, request);
		return new SuccessResponse<RequestDetail>()
				.payload(worklist).get(HttpStatus.OK);
	} 
	
	@Operation(summary = "Retrieve list of workitems for logged in user")
	@GetMapping("/workitem/list/self")
	public ResponseEntity<SuccessResponse<Paginate<WorkDetail>>> getMyWorkItems(
			@RequestParam(required = false) Integer pageIndex, 
			@RequestParam(required = false) Integer pageSize,
			WorkDetailFilter filter) throws Exception {
		Paginate<WorkDetail> requestList=requestBL.getMyWorkList(pageIndex, pageSize,filter);
		return new SuccessResponse<Paginate<WorkDetail>>()
				.payload(requestList).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Retrieves list of workitems against a specific request",description = "Authorities : "+Authority.READ_WORK)
	@PreAuthorize(Authority.READ_WORK)
	@GetMapping("/{id}/workitems")
	public ResponseEntity<SuccessResponse<List<WorkDetail>>> getWorkItems(
			@PathVariable String id) throws Exception {
		List<WorkDetail> requestList=requestBL.getWorkLists(id);
		return new SuccessResponse<List<WorkDetail>>()
				.payload(requestList).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Update an existing workitem",description = "Authorities : "+Authority.UPDATE_WORK)
	@PreAuthorize(Authority.UPDATE_WORK)
	@PatchMapping("/workitem/{id}/update")
	public ResponseEntity<SuccessResponse<WorkDetail>> updateWorkItem(@PathVariable String id,@RequestBody WorkDetail request) throws Exception {
		WorkDetail worklist=requestBL.updateWorkList(id, request);
		return new SuccessResponse<WorkDetail>()
				.payload(worklist).get(HttpStatus.OK);
	} 

}
