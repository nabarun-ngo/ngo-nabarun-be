package ngo.nabarun.app.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.infra.core.entity.TicketInfoEntity;
import ngo.nabarun.app.infra.core.repo.TicketRepository;

@Profile("!prod")
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,value = "/test")
@RestController
public class TestController {
	
	@Autowired
	private TicketRepository ticketRepository;
	
	@GetMapping("/getLatestTestOtp")
	public ResponseEntity<SuccessResponse<String>> getLatestTestOtp(
			@RequestParam String email
			) throws Exception {
		List<TicketInfoEntity> tickets=ticketRepository.findByEmail(email);
		return new SuccessResponse<String>().payload(tickets.isEmpty()? null : tickets.get(tickets.size()-1).getOneTimePassword()).get(HttpStatus.OK);
	}
	
}
