package ngo.nabarun.app.businesslogic;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;

@Service
public interface IAccountBL {

	Paginate<AccountDetail> getAccounts(Integer page,Integer size,AccountDetailFilter filter);
	AccountDetail createAccount(AccountDetailCreate accountDetail) throws Exception;
	AccountDetail updateAccount(String id,AccountDetailUpdate accountDetail) throws Exception;
	Paginate<TransactionDetail> getTransactions(String id, int index, int size);

}
