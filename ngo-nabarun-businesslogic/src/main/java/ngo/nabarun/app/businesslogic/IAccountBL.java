package ngo.nabarun.app.businesslogic;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail.ExpenseDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail.TransactionDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;

@Service
public interface IAccountBL {
 
	Paginate<AccountDetail> getAccounts(Integer page,Integer size,AccountDetailFilter filter);
	AccountDetail updateAccount(String id,AccountDetail accountDetail) throws Exception;
	Paginate<TransactionDetail> getTransactions(String id,Integer index, Integer size,TransactionDetailFilter filter);
	Paginate<TransactionDetail> getMyTransactions(String id,Integer index, Integer size,TransactionDetailFilter filter) throws Exception;
	AccountDetail createAccount(AccountDetail accountDetail) throws Exception;
	Paginate<AccountDetail> getMyAccounts(Integer pageIndex, Integer pageSize, AccountDetailFilter filter) throws Exception;
	TransactionDetail createTransaction(TransactionDetail txnDetail) throws Exception;
	AccountDetail updateMyAccount(String id, AccountDetail accountDetail) throws Exception;
	ExpenseDetail createExpense(ExpenseDetail expense) throws Exception;
	Paginate<ExpenseDetail> getExpenseList(Integer index, Integer size,ExpenseDetailFilter filter);
	ExpenseDetail updateExpense(String id,ExpenseDetail expense) throws Exception;
	ExpenseDetail finalizeExpense(String id, ExpenseDetail expenseDetail) throws Exception;
	ExpenseDetail settleExpense(String id, ExpenseDetail expenseDetail) throws Exception;
	Paginate<ExpenseDetail> getMyExpenseList(Integer index, Integer size, ExpenseDetailFilter filter);

}
