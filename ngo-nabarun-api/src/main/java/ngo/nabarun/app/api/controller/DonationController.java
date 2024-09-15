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
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/donation")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class DonationController {

	@Autowired
	private IDonationBL donationBL;

	@GetMapping("/getDonations")
	public ResponseEntity<SuccessResponse<Paginate<DonationDetail>>> getDonations(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			DonationDetailFilter filter) {
		return new SuccessResponse<Paginate<DonationDetail>>()
				.payload(donationBL.getDonations(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}

	@GetMapping("/getLoggedInUserDonation")
	public ResponseEntity<SuccessResponse<Paginate<DonationDetail>>> getLoggedInUserDonations(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize)
			throws Exception {
		return new SuccessResponse<Paginate<DonationDetail>>()
				.payload(donationBL.getLoggedInUserDonations(pageIndex, pageSize)).get(HttpStatus.OK);
	}

	@GetMapping("/getUserDonation/{id}")
	public ResponseEntity<SuccessResponse<Paginate<DonationDetail>>> getUserDonations(@PathVariable String id,
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize)
			throws Exception {
		return new SuccessResponse<Paginate<DonationDetail>>()
				.payload(donationBL.getUserDonations(id, pageIndex, pageSize)).get(HttpStatus.OK);
	}

	@GetMapping("/getDonationSummary")
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

	@Deprecated
	@GetMapping("/getDonationDocuments/{id}")
	public ResponseEntity<SuccessResponse<List<DocumentDetail>>> getDonationDocuments(@PathVariable String id)
			throws Exception {
		return new SuccessResponse<List<DocumentDetail>>().payload(donationBL.getDonationDocument(id))
				.get(HttpStatus.OK);
	}

	@PostMapping("/raiseDonation")
	public ResponseEntity<SuccessResponse<DonationDetail>> raiseDonation(@RequestBody DonationDetail request)
			throws Exception {
		return new SuccessResponse<DonationDetail>().payload(donationBL.raiseDonation(request)).get(HttpStatus.OK);
	}

	@PostMapping("/payments/{id}")
	public ResponseEntity<SuccessResponse<DonationDetail>> payments(
			@PathVariable String id,
			@RequestParam String action,
			@RequestBody DonationDetail paymentOptions) throws Exception {
		return new SuccessResponse<DonationDetail>().payload(donationBL.updatePaymentInfo(id, paymentOptions)).get(HttpStatus.OK);
	}

	@PatchMapping("/updateDonation/{id}")
	public ResponseEntity<SuccessResponse<DonationDetail>> updateDonation(@PathVariable String id,
			@RequestBody DonationDetail request) throws Exception {
		return new SuccessResponse<DonationDetail>().payload(donationBL.updateDonation(id, request)).get(HttpStatus.OK);
	}

}
