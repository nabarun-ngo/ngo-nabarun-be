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

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/request")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class RequestController {

	@Autowired
	private IRequestBL requestBL;

	@GetMapping("/getMyRequests")
	public ResponseEntity<SuccessResponse<Paginate<RequestDetail>>> getMyRequests(
			@RequestParam(required = false) Integer pageIndex, 
			@RequestParam(required = false) Integer pageSize,
			RequestDetailFilter filter) throws Exception {
		Paginate<RequestDetail> requestList=requestBL.getMyRequests(pageIndex, pageSize,filter);
		return new SuccessResponse<Paginate<RequestDetail>>()
				.payload(requestList).get(HttpStatus.OK);
	}
	
	@GetMapping("/getRequest/{id}")
	public ResponseEntity<SuccessResponse<RequestDetail>> getRequestDetail(@PathVariable String id) throws Exception {
		return new SuccessResponse<RequestDetail>()
				.payload(requestBL.getRequest(id)).get(HttpStatus.OK); 
	}
	
	
	@PostMapping("/createRequest")
	public ResponseEntity<SuccessResponse<RequestDetail>> createRequest(@RequestBody RequestDetail createRequest) throws Exception {
		return new SuccessResponse<RequestDetail>()
				.payload(requestBL.createRequest(createRequest)).get(HttpStatus.OK); 
	}
	
	@PatchMapping("/updateRequest/{id}")
	public ResponseEntity<SuccessResponse<RequestDetail>> updateRequest(@PathVariable String id,@RequestBody RequestDetail request) throws Exception {
		RequestDetail worklist=requestBL.updateRequest(id, request);
		return new SuccessResponse<RequestDetail>()
				.payload(worklist).get(HttpStatus.OK);
	} 
	
	@GetMapping("/getMyWorkItems")
	public ResponseEntity<SuccessResponse<Paginate<WorkDetail>>> getMyWorkItems(
			@RequestParam(required = false) Integer pageIndex, 
			@RequestParam(required = false) Integer pageSize,
			WorkDetailFilter filter) throws Exception {
		Paginate<WorkDetail> requestList=requestBL.getMyWorkList(pageIndex, pageSize,filter);
		return new SuccessResponse<Paginate<WorkDetail>>()
				.payload(requestList).get(HttpStatus.OK);
	}
	
	@GetMapping("/{id}/getWorkItems")
	public ResponseEntity<SuccessResponse<List<WorkDetail>>> getWorkItems(
			@PathVariable String id) throws Exception {
		List<WorkDetail> requestList=requestBL.getWorkLists(id);
		return new SuccessResponse<List<WorkDetail>>()
				.payload(requestList).get(HttpStatus.OK);
	}
	
	@PatchMapping("/updateWorkItem/{id}")
	public ResponseEntity<SuccessResponse<WorkDetail>> updateWorkItem(@PathVariable String id,@RequestBody WorkDetail request) throws Exception {
		WorkDetail worklist=requestBL.updateWorkList(id, request);
		return new SuccessResponse<WorkDetail>()
				.payload(worklist).get(HttpStatus.OK);
	} 

}
