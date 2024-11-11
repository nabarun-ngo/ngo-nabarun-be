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
import ngo.nabarun.app.api.helper.Authority;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary;
import ngo.nabarun.app.businesslogic.businessobjects.HistoryDetail;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/donation")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class DonationController {

	@Autowired
	private IDonationBL donationBL;

	@PreAuthorize(Authority.CREATE_DONATION)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<DonationDetail>> raiseDonation(@RequestBody DonationDetail request)
			throws Exception {
		return new SuccessResponse<DonationDetail>().payload(donationBL.raiseDonation(request)).get(HttpStatus.OK);
	}
	
	@PostMapping("/{id}/payment")
	public ResponseEntity<SuccessResponse<DonationDetail>> payments(
			@PathVariable String id,
			@RequestParam String action,
			@RequestBody DonationDetail paymentOptions) throws Exception {
		return new SuccessResponse<DonationDetail>().payload(donationBL.updatePaymentInfo(id, paymentOptions)).get(HttpStatus.OK);
	}
	
	@PreAuthorize(Authority.READ_DONATIONS)
	@GetMapping("/list")
	public ResponseEntity<SuccessResponse<Paginate<DonationDetail>>> getDonations(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			DonationDetailFilter filter) {
		return new SuccessResponse<Paginate<DonationDetail>>()
				.payload(donationBL.getDonations(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}
	
	@GetMapping("/list/self")
	public ResponseEntity<SuccessResponse<Paginate<DonationDetail>>> getLoggedInUserDonations(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize)
			throws Exception {
		return new SuccessResponse<Paginate<DonationDetail>>()
				.payload(donationBL.getLoggedInUserDonations(pageIndex, pageSize)).get(HttpStatus.OK);
	}
	
	@PreAuthorize(Authority.READ_DONATIONS_GUEST)
	@GetMapping("/list/guest")
	public ResponseEntity<SuccessResponse<Paginate<DonationDetail>>> getGuestDonations(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			DonationDetailFilter filter) {
		filter.setIsGuest(true);
		return new SuccessResponse<Paginate<DonationDetail>>()
				.payload(donationBL.getDonations(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.READ_USER_DONATIONS)
	@GetMapping("/donor/{id}/list")
	public ResponseEntity<SuccessResponse<Paginate<DonationDetail>>> getUserDonations(@PathVariable String id,
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize)
			throws Exception {
		return new SuccessResponse<Paginate<DonationDetail>>()
				.payload(donationBL.getUserDonations(id, pageIndex, pageSize)).get(HttpStatus.OK);
	}

	@GetMapping("/summary")
	public ResponseEntity<SuccessResponse<DonationSummary>> getDonationSummary(
			@RequestParam(required = false) String id, 
			@RequestParam(required = false) boolean includePayableAccount,
			@RequestParam(required = false) boolean includeOutstandingMonths
			)
			throws Exception {
		
		List<String> fields = new ArrayList<String>();
		if (includePayableAccount) {
			fields.add("INCLUDE_PAYABLE_ACCOUNT");
		}
		if (includeOutstandingMonths) {
			fields.add("INCLUDE_OUTSTANDING_MONTHS");
		}
		return new SuccessResponse<DonationSummary>().payload(donationBL.getDonationSummary(id, fields))
				.get(HttpStatus.OK);
	}

	@GetMapping("/{id}/documents")
	public ResponseEntity<SuccessResponse<List<DocumentDetail>>> getDonationDocuments(@PathVariable String id)
			throws Exception {
		return new SuccessResponse<List<DocumentDetail>>().payload(donationBL.getDonationDocument(id))
				.get(HttpStatus.OK);
	}
	
	@GetMapping("/{id}/histories")
	public ResponseEntity<SuccessResponse<List<HistoryDetail>>> getHistories(@PathVariable String id)
			throws Exception {
		return new SuccessResponse<List<HistoryDetail>>().payload(donationBL.getHistories(id))
				.get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.UPDATE_DONATION)
	@PatchMapping("/{id}/update")
	public ResponseEntity<SuccessResponse<DonationDetail>> updateDonation(@PathVariable String id,
			@RequestBody DonationDetail request) throws Exception {
		return new SuccessResponse<DonationDetail>().payload(donationBL.updateDonation(id, request)).get(HttpStatus.OK);
	}
	
}
