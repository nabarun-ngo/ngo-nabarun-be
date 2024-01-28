package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;

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
	public ResponseEntity<SuccessResponse<AccountDetail>> createAccount(@RequestBody AccountDetailCreate accountDetail)
			throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.createAccount(accountDetail)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getAccounts")
	public ResponseEntity<SuccessResponse<Paginate<AccountDetail>>> getAccounts()
			throws Exception {
		return new SuccessResponse<Paginate<AccountDetail>>().payload(accountBL.getAccounts(null, null)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getTransactions/{id}")
	public ResponseEntity<SuccessResponse<Object>> getTransactions(@PathVariable String id)
			throws Exception {
		accountBL.getTransactions(id);
		return new SuccessResponse<Object>().payload(null).get(HttpStatus.OK);
	}
}
