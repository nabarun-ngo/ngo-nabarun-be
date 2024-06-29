package ngo.nabarun.app.businesslogic.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;
import ngo.nabarun.app.businesslogic.domain.AccountDO;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;

@Service
public class AccountBLImpl implements IAccountBL {

	@Autowired
	private AccountDO accountDO;

	@Override
	public Paginate<AccountDetail> getAccounts(Integer page, Integer size, AccountDetailFilter filter) {
		return accountDO.retrieveAccounts(page, size, filter).map(BusinessObjectConverter::toAccountDetail);
	}

	@Override
	public AccountDetail createAccount(AccountDetail accountDetail) throws Exception {
		return BusinessObjectConverter.toAccountDetail(accountDO.createAccount(accountDetail, 0.0));
	}

	@Override
	public AccountDetail updateAccount(String id, AccountDetail accountDetail) throws Exception {
		return null;
	}

	@Override
	public Paginate<TransactionDetail> getTransactions(String id, int index, int size) {
		return accountDO.retrieveAccountTransactions(id, index, size)
				.map(m -> BusinessObjectConverter.toTransactionDetail(m, false));
	}

}
