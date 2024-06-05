package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/account")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class AccountController {

	@Autowired
	private IAccountBL accountBL;

	@PostMapping("/createAccount")
	public ResponseEntity<SuccessResponse<AccountDetail>> createAccount(@RequestBody AccountDetail accountDetail)
			throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.createAccount(accountDetail)).get(HttpStatus.OK);
	}

	@GetMapping("/getAccounts")
	public ResponseEntity<SuccessResponse<Paginate<AccountDetail>>> getAccounts(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			AccountDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<AccountDetail>>()
				.payload(accountBL.getAccounts(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}

	@GetMapping("/{id}/getTransactions")
	public ResponseEntity<SuccessResponse<Paginate<TransactionDetail>>> getTransactions(
			@PathVariable String id,
			@RequestParam(required = true) Integer pageIndex, 
			@RequestParam(required = true) Integer pageSize
			) throws Exception {
		return new SuccessResponse<Paginate<TransactionDetail>>().payload(accountBL.getTransactions(id, pageIndex, pageSize)).get(HttpStatus.OK);
	}
}
