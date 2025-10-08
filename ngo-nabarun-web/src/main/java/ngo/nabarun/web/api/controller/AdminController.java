package ngo.nabarun.web.api.controller;

import lombok.RequiredArgsConstructor;
import ngo.nabarun.application.dto.result.OutboxEventResult;
import ngo.nabarun.application.service.AdminService;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;
import ngo.nabarun.web.api.dto.SuccessResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class AdminController {
	@Autowired
	private AdminService adminService;

	@GetMapping("/listOutbox")
	public ResponseEntity<SuccessResponse<List<OutboxEventResult>>> listOutbox(@RequestParam OutboxStatus status) {
		return new SuccessResponse<List<OutboxEventResult>>().payload(adminService.getOutboxEvents(status))
				.get(HttpStatus.OK);
	}
	
	@GetMapping("/outbox/{id}")
	public ResponseEntity<SuccessResponse<OutboxEventResult>> getOutbox(@PathVariable String id) {
		return new SuccessResponse<OutboxEventResult>().payload(adminService.getOutboxEvent(id))
				.get(HttpStatus.OK);
	}


	@PostMapping("/processOutbox/{id}")
	public ResponseEntity<SuccessResponse<Void>> processOutbox(@PathVariable String id) {
		adminService.processFromOutbox(id);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@PostMapping("/processPendingOutbox")
	public ResponseEntity<SuccessResponse<Void>> processPendingOutbox(@RequestParam(defaultValue = "10") int limit) {
		adminService.retryPendingEvents(limit);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
}