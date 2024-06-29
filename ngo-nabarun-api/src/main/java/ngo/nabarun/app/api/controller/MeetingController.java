package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IMeetingBL;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetail;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetailCreate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/meeting")
@SecurityRequirement(name = "nabarun_auth")
public class MeetingController {

	@Autowired
	private IMeetingBL meetingBL;
	
	
	@PostMapping("/createMeeting")
	public ResponseEntity<SuccessResponse<MeetingDetail>> authorizeAndCreateMeeting(@RequestBody MeetingDetailCreate meetingDetail)
			throws Exception {
		return new SuccessResponse<MeetingDetail>().payload(meetingBL.createMeeting(meetingDetail))
				.get(HttpStatus.OK);
	}


}
