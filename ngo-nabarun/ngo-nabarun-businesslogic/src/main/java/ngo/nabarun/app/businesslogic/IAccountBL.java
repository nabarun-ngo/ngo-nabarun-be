package ngo.nabarun.app.businesslogic;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Page;

@Service
public interface IAccountBL {

	Page<AccountDetail> getAccounts(Integer page,Integer size);
	AccountDetail createAccount(AccountDetailCreate accountDetail) throws Exception;
	AccountDetail updateAccount(String id,AccountDetailUpdate accountDetail) throws Exception;
	void getTransactions(String id);

}
