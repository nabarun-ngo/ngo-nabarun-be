package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.converter.RequestConverter;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Page;
import ngo.nabarun.app.businesslogic.businessobjects.PaymentOptions;
import ngo.nabarun.app.common.util.CommonUtils;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/donation")
@SecurityRequirement(name = "nabarun_auth")
public class DonationController {

	@Autowired
	private IDonationBL donationBL;

	@GetMapping("/getDonations")
	public ResponseEntity<SuccessResponse<Page<DonationDetail>>> getDonations(
			@RequestParam(required = false) Integer pageIndex,
			@RequestParam(required = false) Integer pageSize, 
			@RequestParam(required = false) String filter) {
		
		DonationDetailFilter filterObject = null;
		if(filter != null) {
			filterObject = CommonUtils.jsonToPojo(filter, DonationDetailFilter.class);
		}
		return new SuccessResponse<Page<DonationDetail>>().payload(donationBL.getDonations(pageIndex,pageSize,filterObject)).get(HttpStatus.OK);
	}

	@GetMapping("/getLoggedInUserDonation")
	public ResponseEntity<SuccessResponse<List<DonationDetail>>> getLoggedInUserDonations() throws Exception {
		return new SuccessResponse<List<DonationDetail>>().payload(donationBL.getLoggedInUserDonations())
				.get(HttpStatus.OK);
	}

	@GetMapping("/getUserDonation/{id}")
	public ResponseEntity<SuccessResponse<List<DonationDetail>>> getUserDonations(@PathVariable String id) throws Exception {
		return new SuccessResponse<List<DonationDetail>>().payload(donationBL.getUserDonations(id)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getDonationDocuments/{id}")
	public ResponseEntity<SuccessResponse<List<DocumentDetail>>> getDonationDocuments(@PathVariable String id) throws Exception {
		return new SuccessResponse<List<DocumentDetail>>().payload(donationBL.getDonationDocument(id)).get(HttpStatus.OK);
	}


	@PostMapping("/payNow")
	public ResponseEntity<SuccessResponse<List<DonationDetail>>> payLoggedInUserDonation(
			@RequestBody PaymentOptions paymentOptions) {

		return new SuccessResponse<List<DonationDetail>>().payload(null).get(HttpStatus.OK);
	}

	@PostMapping("/raiseDonation")
	public ResponseEntity<SuccessResponse<DonationDetail>> raiseDonation(@RequestBody DonationDetailCreate request)
			throws Exception {
		return new SuccessResponse<DonationDetail>()
				.payload(donationBL.raiseDonation(request))
				.get(HttpStatus.OK);
	}
	
	

}
