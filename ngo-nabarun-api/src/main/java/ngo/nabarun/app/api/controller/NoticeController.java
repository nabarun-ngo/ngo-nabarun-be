package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
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
import ngo.nabarun.app.businesslogic.INoticeBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.util.CommonUtils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,value = "/api/notice")
@SecurityRequirement(name = "nabarun_auth")
public class NoticeController {
	
	@Autowired
	private INoticeBL noticeBL;
	


	@GetMapping("/getNotices")
	public ResponseEntity<SuccessResponse<Paginate<NoticeDetail>>> getAllNotice(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String filter) throws Exception {
		NoticeDetailFilter noticeFilter = null;
		if (filter != null) {
			noticeFilter = CommonUtils.jsonToPojo(filter, NoticeDetailFilter.class);
		}
		return new SuccessResponse<Paginate<NoticeDetail>>()
				.payload(noticeBL.getAllNotice(pageIndex, pageSize, noticeFilter)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getDraftedNotice")
	public ResponseEntity<SuccessResponse<NoticeDetail>> getDraftedNotice() throws Exception {
		return new SuccessResponse<NoticeDetail>()
				.payload(noticeBL.getDraftedNotice()).get(HttpStatus.OK);
	}

	@GetMapping("/getNotice/{id}")
	public ResponseEntity<SuccessResponse<NoticeDetail>> getNotice(@PathVariable String id) throws Exception {
		return new SuccessResponse<NoticeDetail>().payload(noticeBL.getNoticeDetail(id)).get(HttpStatus.OK);
	}

	@PostMapping("/createNotice")
	public ResponseEntity<SuccessResponse<NoticeDetail>> createNotice(@RequestBody NoticeDetailCreate noticeDetail)
			throws Exception {
		return new SuccessResponse<NoticeDetail>().payload(noticeBL.createNotice(noticeDetail))
				.get(HttpStatus.OK);
	}

	@PatchMapping("/updateNotice/{id}")
	public ResponseEntity<SuccessResponse<NoticeDetail>> updateNotice(@PathVariable String id,
			@RequestBody NoticeDetailUpdate noticeDetail) throws Exception {
		return new SuccessResponse<NoticeDetail>().payload(noticeBL.updateNotice(id, noticeDetail))
				.get(HttpStatus.OK);
	}

	@GetMapping("/getNoticeDocuments/{id}")
	public ResponseEntity<SuccessResponse<List<DocumentDetail>>> getNoticeDocuments(@PathVariable String id)
			throws Exception {
		return new SuccessResponse<List<DocumentDetail>>().payload(noticeBL.getNoticeDocs(id))
				.get(HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteNotice/{id}")
	public ResponseEntity<SuccessResponse<Void>> deleteEvent(@PathVariable String id)
			throws Exception {
		noticeBL.deleteNotice(id);
		return new SuccessResponse<Void>()
				.get(HttpStatus.OK);
	}
}
