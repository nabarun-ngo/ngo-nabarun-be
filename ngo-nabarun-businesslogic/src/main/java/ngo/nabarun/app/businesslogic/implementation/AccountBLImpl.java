package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail.ExpenseDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail.TransactionDetailFilter;
import ngo.nabarun.app.businesslogic.domain.AccountDO;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.ExpenseDTO;
import ngo.nabarun.app.infra.dto.ExpenseDTO.ExpenseDTOFilter;
import ngo.nabarun.app.infra.dto.TransactionDTO.TransactionDTOFilter;
import ngo.nabarun.app.infra.dto.TransactionDTO;

@Service
public class AccountBLImpl extends BaseBLImpl implements IAccountBL {

	@Autowired
	private AccountDO accountDO;

	@Override
	public Paginate<AccountDetail> getAccounts(Integer page, Integer size, AccountDetailFilter filter) {
		return accountDO.retrieveAccounts(page, size, filter).map(BusinessObjectConverter::toAccountDetail);
	}

	@Override
	public AccountDetail createAccount(AccountDetail accountDetail) throws Exception {
		return BusinessObjectConverter.toAccountDetail(accountDO.createAccount(accountDetail,
				accountDetail.getCurrentBalance(), SecurityUtils.getAuthUserId()));
	}

	@Override
	public AccountDetail updateAccount(String id, AccountDetail accountDetail) throws Exception {
		AccountDetail updatedAccountDetail = new AccountDetail();
		updatedAccountDetail.setAccountStatus(accountDetail.getAccountStatus());
		return BusinessObjectConverter.toAccountDetail(accountDO.updateAccount(id, updatedAccountDetail));
	}

	@Override
	public AccountDetail updateMyAccount(String id, AccountDetail accountDetail) throws Exception {
		AccountDetail updatedAccountDetail = new AccountDetail();
		updatedAccountDetail.setUpiDetail(accountDetail.getUpiDetail());
		updatedAccountDetail.setBankDetail(accountDetail.getBankDetail());
		return BusinessObjectConverter.toAccountDetail(accountDO.updateAccount(id, updatedAccountDetail));
	}

	@Override
	public Paginate<TransactionDetail> getMyTransactions(String id, Integer index, Integer size,
			TransactionDetailFilter filter) throws Exception {
		List<AccountDTO> myaccounts = accountDO.retrieveMyAccounts(null, null, new AccountDetailFilter()).getContent();
		myaccounts.stream().filter(f -> f.getId().equalsIgnoreCase(id)).findFirst()
				.orElseThrow(() -> new Exception("Invalid account selected"));
		TransactionDTOFilter filterDTO = new TransactionDTOFilter();
		filterDTO.setAccountId(id);
		filterDTO.setTxnId(filter.getTxnId());
		filterDTO.setFromDate(filter.getStartDate());
		filterDTO.setToDate(filter.getEndDate());
		filterDTO.setTxnRefType(filter.getTxnRefType());
		filterDTO.setTxnStatus(filter.getTxnStatus());
		filterDTO.setTxnType(filter.getTxnType());
		filterDTO.setTxnRefId(filter.getTxnRefId());
		return accountDO.retrieveAccountTransactions(id, index, size, filterDTO)
				.map(m -> BusinessObjectConverter.toTransactionDetail(m, true, id));
	}

	@Override
	public Paginate<TransactionDetail> getTransactions(String id, Integer index, Integer size,
			TransactionDetailFilter filter) {
		TransactionDTOFilter filterDTO = new TransactionDTOFilter();
		filterDTO.setAccountId(id);
		filterDTO.setTxnId(filter.getTxnId());
		filterDTO.setFromDate(filter.getStartDate());
		filterDTO.setToDate(filter.getEndDate());
		filterDTO.setTxnRefType(filter.getTxnRefType());
		filterDTO.setTxnStatus(filter.getTxnStatus());
		filterDTO.setTxnType(filter.getTxnType());
		filterDTO.setTxnRefId(filter.getTxnRefId());
		return accountDO.retrieveAccountTransactions(id, index, size, filterDTO)
				.map(m -> BusinessObjectConverter.toTransactionDetail(m, true, id));
	}

	@Override
	public TransactionDetail createTransaction(TransactionDetail txnDetail) throws Exception {
		if (txnDetail.getTxnType() == TransactionType.TRANSFER
				&& txnDetail.getTransferFrom().getId().equals(txnDetail.getTransferTo().getId())) {
			throw new Exception("From and to accounts are same.");
		}
		AccountDetailFilter accountFilter=new AccountDetailFilter();
		accountFilter.setIncludeBalance(true);
		List<AccountDTO> myaccounts = accountDO.retrieveMyAccounts(null, null,accountFilter ).getContent();
		AccountDTO myAccount;
		if (txnDetail.getTxnType() == TransactionType.IN) {
			myAccount = myaccounts.stream().filter(f -> f.getId().equalsIgnoreCase(txnDetail.getTransferTo().getId()))
					.findFirst().orElseThrow(() -> new Exception("Invalid account selected"));
		} else {// TransactionType.OUT || TransactionType.TRANSFER
			myAccount = myaccounts.stream().filter(f -> f.getId().equalsIgnoreCase(txnDetail.getTransferFrom().getId()))
					.findFirst().orElseThrow(() -> new Exception("Invalid account selected"));
			// Validate account balance
			businessHelper.throwBusinessExceptionIf(() -> txnDetail.getTxnAmount() > myAccount.getCurrentBalance(),
					ExceptionEvent.INSUFFICIENT_ACCOUNT_BALANCE);
		}
		txnDetail.setTxnDate(txnDetail.getTxnDate() != null ?txnDetail.getTxnDate() : CommonUtils.getSystemDate());
		TransactionDTO transaction = accountDO.createTransaction(txnDetail, myAccount.getProfile());
		return BusinessObjectConverter.toTransactionDetail(transaction, true, null);
	}

	@Override
	public Paginate<AccountDetail> getMyAccounts(Integer pageIndex, Integer pageSize, AccountDetailFilter filter)
			throws Exception {
		return accountDO.retrieveMyAccounts(pageIndex, pageSize, filter).map(BusinessObjectConverter::toAccountDetail);
	}

	@Override
	public ExpenseDetail createExpense(ExpenseDetail expense) throws Exception {
		ExpenseDTO expenseDTO = accountDO.createExpense(expense);
		return BusinessObjectConverter.toExpenseDetail(expenseDTO);
	}

	@Override
	public Paginate<ExpenseDetail> getExpenseList(Integer index, Integer size, ExpenseDetailFilter filter) {
		ExpenseDTOFilter filterDTO = new ExpenseDTOFilter();
		filterDTO.setStartDate(filter.getStartDate());
		filterDTO.setEndDate(filter.getEndDate());
		filterDTO.setRefId(filter.getExpenseRefId());
		filterDTO.setExpId(filter.getExpenseId());
		filterDTO.setStatus(filter.getExpenseStatus());
		filterDTO.setPayerId(filter.getPayerId());
		Paginate<ExpenseDTO> expenseDTOs = accountDO.getExpenses(index, size, filterDTO);
		return expenseDTOs.map(BusinessObjectConverter::toExpenseDetail);
	}

	@Override
	public Paginate<ExpenseDetail> getMyExpenseList(Integer index, Integer size, ExpenseDetailFilter filter) {
		ExpenseDTOFilter filterDTO = new ExpenseDTOFilter();
		filterDTO.setStartDate(filter.getStartDate());
		filterDTO.setEndDate(filter.getEndDate());
		filterDTO.setRefId(filter.getExpenseRefId());
		filterDTO.setExpId(filter.getExpenseId());
		filterDTO.setStatus(filter.getExpenseStatus());
		filterDTO.setPayerId(SecurityUtils.getAuthUser().getId());
		Paginate<ExpenseDTO> expenseDTOs = accountDO.getExpenses(index, size, filterDTO);
		return expenseDTOs.map(BusinessObjectConverter::toExpenseDetail);
	}

	@Override
	public ExpenseDetail updateExpense(String id, ExpenseDetail expense) throws Exception {
		ExpenseDTO expenseDTO = accountDO.updateExpense(id, expense, "update");
		return BusinessObjectConverter.toExpenseDetail(expenseDTO);
	}

	@Override
	public ExpenseDetail finalizeExpense(String id, ExpenseDetail expenseDetail) throws Exception {
		ExpenseDTO expenseDTO = accountDO.updateExpense(id, expenseDetail, "finalize");
		return BusinessObjectConverter.toExpenseDetail(expenseDTO);
	}

	@Override
	public ExpenseDetail settleExpense(String id, ExpenseDetail expenseDetail) throws Exception {
		ExpenseDTO expenseDTO = accountDO.updateExpense(id, expenseDetail, "settle");
		return BusinessObjectConverter.toExpenseDetail(expenseDTO);
	}

}
