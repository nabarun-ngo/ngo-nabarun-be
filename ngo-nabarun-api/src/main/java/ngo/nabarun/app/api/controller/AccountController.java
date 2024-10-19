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
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail.ExpenseDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail.TransactionDetailFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/account")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class AccountController {

	@Autowired
	private IAccountBL accountBL;

	@PreAuthorize(Authority.CREATE_ACCOUNT)
	@PostMapping("/createAccount")
	public ResponseEntity<SuccessResponse<AccountDetail>> createAccount(@RequestBody AccountDetail accountDetail)
			throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.createAccount(accountDetail)).get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.READ_ACCOUNTS)
	@GetMapping("/getAccounts")
	public ResponseEntity<SuccessResponse<Paginate<AccountDetail>>> getAccounts(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			AccountDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<AccountDetail>>()
				.payload(accountBL.getAccounts(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}
	
	

	@GetMapping("/getMyAccounts")
	public ResponseEntity<SuccessResponse<Paginate<AccountDetail>>> getMyAccounts(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			AccountDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<AccountDetail>>()
				.payload(accountBL.getMyAccounts(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}

	@PostMapping("/createTransaction")
	public ResponseEntity<SuccessResponse<TransactionDetail>> createTransaction(
			@RequestBody TransactionDetail txnDetail) throws Exception {
		return new SuccessResponse<TransactionDetail>().payload(accountBL.createTransaction(txnDetail))
				.get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.READ_TRANSACTIONS)
	@GetMapping("/{id}/getTransactions")
	public ResponseEntity<SuccessResponse<Paginate<TransactionDetail>>> getTransactions(@PathVariable String id,
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			TransactionDetailFilter filter) throws Exception {
		filter = filter == null ? new TransactionDetailFilter() : filter;
		return new SuccessResponse<Paginate<TransactionDetail>>()
				.payload(accountBL.getTransactions(id, pageIndex, pageSize, filter)).get(HttpStatus.OK);
	} 

	@GetMapping("/{id}/getMyTransactions")
	public ResponseEntity<SuccessResponse<Paginate<TransactionDetail>>> getMyTransactions(@PathVariable String id,
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			TransactionDetailFilter filter) throws Exception {
		filter = filter == null ? new TransactionDetailFilter() : filter;
		return new SuccessResponse<Paginate<TransactionDetail>>()
				.payload(accountBL.getMyTransactions(id, pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}

	@PatchMapping("/{id}/updateMyAccount")
	public ResponseEntity<SuccessResponse<AccountDetail>> updateMyAccount(@PathVariable String id,
			@RequestBody AccountDetail accDetail) throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.updateMyAccount(id, accDetail))
				.get(HttpStatus.OK);
	}

	@PreAuthorize(Authority.UPDATE_ACCOUNT)
	@PatchMapping("/{id}/updateAccount")
	public ResponseEntity<SuccessResponse<AccountDetail>> updateAccount(@PathVariable String id,
			@RequestBody AccountDetail accDetail) throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.updateAccount(id, accDetail)).get(HttpStatus.OK);
	}
	
	@GetMapping("/getExpenses")
	public ResponseEntity<SuccessResponse<Paginate<ExpenseDetail>>> getExpenses(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			ExpenseDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<ExpenseDetail>>()
				.payload(accountBL.getExpenseList(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}
	
	@PostMapping("/createExpense")
	public ResponseEntity<SuccessResponse<ExpenseDetail>> createExpense(@RequestBody ExpenseDetail expense) throws Exception {
		return new SuccessResponse<ExpenseDetail>()
				.payload(accountBL.createExpenseGroup(expense)).get(HttpStatus.OK);
	}
	
	@PatchMapping("/{id}/updateExpense")
	public ResponseEntity<SuccessResponse<ExpenseDetail>> updateExpense(
			@PathVariable String id,
			@RequestBody ExpenseDetail expense) throws Exception {
		return new SuccessResponse<ExpenseDetail>()
				.payload(accountBL.updateExpenseGroup(id,expense)).get(HttpStatus.OK);
	}
}
