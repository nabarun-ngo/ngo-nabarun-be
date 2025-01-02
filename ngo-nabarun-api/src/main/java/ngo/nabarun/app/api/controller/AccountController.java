package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.helper.Authority;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail.ExpenseDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail.ExpenseItemDetail;
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
public class AccountController {

	@Autowired
	private IAccountBL accountBL;

	@Operation(summary = "Retrieves list of accounts",description = "Authorities : "+Authority.READ_ACCOUNTS)
	@PreAuthorize(Authority.READ_ACCOUNTS)
	@GetMapping("/list")
	public ResponseEntity<SuccessResponse<Paginate<AccountDetail>>> getAccounts(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			AccountDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<AccountDetail>>()
				.payload(accountBL.getAccounts(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}

	@Operation(summary = "Retrieves list of accounts of the logged in user")
	@GetMapping("/list/self")
	public ResponseEntity<SuccessResponse<Paginate<AccountDetail>>> getMyAccounts(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			AccountDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<AccountDetail>>()
				.payload(accountBL.getMyAccounts(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Create new account",description = "Authorities : "+Authority.CREATE_ACCOUNT)
	@PreAuthorize(Authority.CREATE_ACCOUNT)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<AccountDetail>> createAccount(@RequestBody AccountDetail accountDetail)
			throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.createAccount(accountDetail)).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Create new transaction",description = "Authorities : "+Authority.CREATE_TRANSACTION)
	@PreAuthorize(Authority.CREATE_TRANSACTION)
	@PostMapping("/transaction/create")
	public ResponseEntity<SuccessResponse<TransactionDetail>> createTransaction(
			@RequestBody TransactionDetail txnDetail) throws Exception {
		return new SuccessResponse<TransactionDetail>().payload(accountBL.createTransaction(txnDetail))
				.get(HttpStatus.OK);
	}

	@Operation(summary = "Retrieve list of transaction for an account",description = "Authorities : "+Authority.READ_TRANSACTIONS)
	@PreAuthorize(Authority.READ_TRANSACTIONS)
	@GetMapping("/{id}/transaction/list")
	public ResponseEntity<SuccessResponse<Paginate<TransactionDetail>>> getTransactions(@PathVariable String id,
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			TransactionDetailFilter filter) throws Exception {
		filter = filter == null ? new TransactionDetailFilter() : filter;
		return new SuccessResponse<Paginate<TransactionDetail>>()
				.payload(accountBL.getTransactions(id, pageIndex, pageSize, filter)).get(HttpStatus.OK);
	} 

	@Operation(summary = "Retrieve list of transaction for a logged in user account")
	@GetMapping("/{id}/transaction/list/self")
	public ResponseEntity<SuccessResponse<Paginate<TransactionDetail>>> getMyTransactions(@PathVariable String id,
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			TransactionDetailFilter filter) throws Exception {
		filter = filter == null ? new TransactionDetailFilter() : filter;
		return new SuccessResponse<Paginate<TransactionDetail>>()
				.payload(accountBL.getMyTransactions(id, pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}

	@Operation(summary = "Update account details of logged in user")
	@PatchMapping("/{id}/update/self")
	public ResponseEntity<SuccessResponse<AccountDetail>> updateMyAccount(@PathVariable String id,
			@RequestBody AccountDetail accDetail) throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.updateMyAccount(id, accDetail))
				.get(HttpStatus.OK);
	}

	@Operation(summary = "Update account details",description = "Authorities : "+Authority.UPDATE_ACCOUNT)
	@PreAuthorize(Authority.UPDATE_ACCOUNT)
	@PatchMapping("/{id}/update")
	public ResponseEntity<SuccessResponse<AccountDetail>> updateAccount(@PathVariable String id,
			@RequestBody AccountDetail accDetail) throws Exception {
		return new SuccessResponse<AccountDetail>().payload(accountBL.updateAccount(id, accDetail)).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Retrieve list of expenses",description = "Authorities : "+Authority.READ_EXPENSE)
	@PreAuthorize(Authority.READ_EXPENSE)
	@GetMapping("/expense/list")
	public ResponseEntity<SuccessResponse<Paginate<ExpenseDetail>>> getExpenses(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize,
			ExpenseDetailFilter filter) throws Exception {
		return new SuccessResponse<Paginate<ExpenseDetail>>()
				.payload(accountBL.getExpenseList(pageIndex, pageSize, filter)).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Create new expense",description = "Authorities : "+Authority.CREATE_EXPENSE)
	@PreAuthorize(Authority.CREATE_EXPENSE)
	@PostMapping("/expense/create")
	public ResponseEntity<SuccessResponse<ExpenseDetail>> createExpense(@RequestBody ExpenseDetail expense) throws Exception {
		return new SuccessResponse<ExpenseDetail>()
				.payload(accountBL.createExpenseGroup(expense)).get(HttpStatus.OK);
	}
	
	@Operation(summary = "Update details of expense",description = "Authorities : "+Authority.UPDATE_EXPENSE)
	@PreAuthorize(Authority.UPDATE_EXPENSE)
	@PatchMapping("/expense/{id}/update")
	public ResponseEntity<SuccessResponse<ExpenseDetail>> updateExpense(
			@PathVariable String id,
			@RequestBody ExpenseDetail expense) throws Exception {
		return new SuccessResponse<ExpenseDetail>()
				.payload(accountBL.updateExpenseGroup(id,expense)).get(HttpStatus.OK);
	}
	
	@Operation(description = "Authorities : "+Authority.CREATE_EXPENSE_ITEM)
	@PreAuthorize(Authority.CREATE_EXPENSE_ITEM)
	@PostMapping("/expense/{id}/createitem")
	public ResponseEntity<SuccessResponse<ExpenseItemDetail>> createExpenseItem(
			@PathVariable String id,
			@RequestBody ExpenseItemDetail expenseItem) throws Exception {
		return new SuccessResponse<ExpenseItemDetail>()
				.payload(accountBL.createExpenseItem(id,expenseItem)).get(HttpStatus.OK);
	}
}
