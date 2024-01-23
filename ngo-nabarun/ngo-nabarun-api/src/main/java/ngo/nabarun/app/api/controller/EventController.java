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
import ngo.nabarun.app.businesslogic.ISocialEventBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.util.CommonUtils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/socialevent")
@SecurityRequirement(name = "nabarun_auth")
public class EventController {

	@Autowired
	private ISocialEventBL socialEventBL;

	@GetMapping("/getEvents")
	public ResponseEntity<SuccessResponse<Paginate<EventDetail>>> getSocialEvents(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String filter) throws Exception {
		EventDetailFilter eventFilter = null;
		if (filter != null) {
			eventFilter = CommonUtils.jsonToPojo(filter, EventDetailFilter.class);
		}
		return new SuccessResponse<Paginate<EventDetail>>()
				.payload(socialEventBL.getSocialEvents(pageIndex, pageSize, eventFilter)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getDraftedEvent")
	public ResponseEntity<SuccessResponse<EventDetail>> getDraftedEvent() throws Exception {
		return new SuccessResponse<EventDetail>()
				.payload(socialEventBL.getDraftedEvent()).get(HttpStatus.OK);
	}

	@GetMapping("/getEvent/{id}")
	public ResponseEntity<SuccessResponse<EventDetail>> getSocialEvent(@PathVariable String id) throws Exception {
		return new SuccessResponse<EventDetail>().payload(socialEventBL.getSocialEvent(id)).get(HttpStatus.OK);
	}

	@PostMapping("/createEvent")
	public ResponseEntity<SuccessResponse<EventDetail>> createSocialEvent(@RequestBody EventDetailCreate eventDetail)
			throws Exception {
		return new SuccessResponse<EventDetail>().payload(socialEventBL.createSocialEvent(eventDetail))
				.get(HttpStatus.OK);
	}

	@PatchMapping("/updateEvent/{id}")
	public ResponseEntity<SuccessResponse<EventDetail>> updateSocialEvent(@PathVariable String id,
			@RequestBody EventDetailUpdate eventDetail) throws Exception {
		return new SuccessResponse<EventDetail>().payload(socialEventBL.updateSocialEvent(id, eventDetail))
				.get(HttpStatus.OK);
	}

	@GetMapping("/getEventDocuments/{id}")
	public ResponseEntity<SuccessResponse<List<DocumentDetail>>> getSocialEventDocuments(@PathVariable String id)
			throws Exception {
		return new SuccessResponse<List<DocumentDetail>>().payload(socialEventBL.getSocialEventDocs(id))
				.get(HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteEvent/{id}")
	public ResponseEntity<SuccessResponse<Void>> deleteEvent(@PathVariable String id)
			throws Exception {
		socialEventBL.deleteEvent(id);
		return new SuccessResponse<Void>()
				.get(HttpStatus.OK);
	}

}
