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
import ngo.nabarun.app.businesslogic.INoticeBL;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail.NoticeDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,value = "/api/notice")
@SecurityRequirement(name = "nabarun_auth")
public class NoticeController {
	
	@Autowired
	private INoticeBL noticeBL;
	
	@Operation(summary = "Retrieve notice list",description = "Authorities : "+Authority.READ_NOTICES)
	@PreAuthorize(Authority.READ_NOTICES)
	@GetMapping("/list")
	public ResponseEntity<SuccessResponse<Paginate<NoticeDetail>>> getAllNotice(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			NoticeDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<NoticeDetail>>()
				.payload(noticeBL.getAllNotice(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}
	
	@Deprecated
	@GetMapping("/getDraftedNotice")
	public ResponseEntity<SuccessResponse<NoticeDetail>> getDraftedNotice() throws Exception {
		return new SuccessResponse<NoticeDetail>()
				.payload(noticeBL.getDraftedNotice()).get(HttpStatus.OK);
	}

	@Operation(summary = "Retrieve details of a specific notice",description = "Authorities : "+Authority.READ_NOTICE)
	@PreAuthorize(Authority.READ_NOTICE)
	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<NoticeDetail>> getNotice(@PathVariable String id) throws Exception {
		return new SuccessResponse<NoticeDetail>().payload(noticeBL.getNoticeDetail(id)).get(HttpStatus.OK);
	}

	@Operation(summary = "Create a new notice",description = "Authorities : "+Authority.CREATE_NOTICE)
	@PreAuthorize(Authority.CREATE_NOTICE)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<NoticeDetail>> createNotice(@RequestBody NoticeDetail noticeDetail)
			throws Exception {
		return new SuccessResponse<NoticeDetail>().payload(noticeBL.createNotice(noticeDetail))
				.get(HttpStatus.OK);
	}

	@Operation(summary = "Update a specific notice",description = "Authorities : "+Authority.UPDATE_NOTICE)
	@PreAuthorize(Authority.UPDATE_NOTICE)
	@PatchMapping("/{id}/update")
	public ResponseEntity<SuccessResponse<NoticeDetail>> updateNotice(@PathVariable String id,
			@RequestBody NoticeDetail noticeDetail) throws Exception {
		return new SuccessResponse<NoticeDetail>().payload(noticeBL.updateNotice(id, noticeDetail))
				.get(HttpStatus.OK);
	}

//	@GetMapping("/getNoticeDocuments/{id}")
//	public ResponseEntity<SuccessResponse<List<DocumentDetail>>> getNoticeDocuments(@PathVariable String id)
//			throws Exception {
//		return new SuccessResponse<List<DocumentDetail>>().payload(noticeBL.getNoticeDocs(id))
//				.get(HttpStatus.OK);
//	}
	
//	@PreAuthorize(Authority.DELETE_NOTICE)
//	@DeleteMapping("/{id}/delete")
//	public ResponseEntity<SuccessResponse<Void>> deleteEvent(@PathVariable String id)
//			throws Exception {
//		noticeBL.deleteNotice(id);
//		return new SuccessResponse<Void>()
//				.get(HttpStatus.OK);
//	}
}
