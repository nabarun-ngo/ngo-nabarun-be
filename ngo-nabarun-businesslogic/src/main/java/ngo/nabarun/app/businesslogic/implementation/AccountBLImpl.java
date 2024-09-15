package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;
import ngo.nabarun.app.businesslogic.domain.AccountDO;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;

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
		return BusinessObjectConverter.toAccountDetail(accountDO.createAccount(accountDetail, accountDetail.getCurrentBalance(), SecurityUtils.getAuthUserId()));
	}

	@Override
	public AccountDetail updateAccount(String id, AccountDetail accountDetail) throws Exception {
		 AccountDetail updatedAccountDetail= new  AccountDetail();
		 updatedAccountDetail.setAccountStatus(accountDetail.getAccountStatus());
		return BusinessObjectConverter.toAccountDetail(accountDO.updateAccount(id,updatedAccountDetail));
	}
	
	@Override
	public AccountDetail updateMyAccount(String id, AccountDetail accountDetail) throws Exception {
		AccountDetail updatedAccountDetail= new  AccountDetail();
		updatedAccountDetail.setUpiDetail(accountDetail.getUpiDetail());
		updatedAccountDetail.setBankDetail(accountDetail.getBankDetail());
		return BusinessObjectConverter.toAccountDetail(accountDO.updateAccount(id,updatedAccountDetail));
	}

	@Override
	public Paginate<TransactionDetail> getTransactions(String id, int index, int size) {
		return accountDO.retrieveAccountTransactions(id, index, size)
				.map(m -> BusinessObjectConverter.toTransactionDetail(m, true,id));
	}
	
	@Override
	public TransactionDetail createTransaction(TransactionDetail txnDetail) throws Exception {
		if(txnDetail.getTxnType() == TransactionType.TRANSFER && txnDetail.getTransferFrom().getId().equals(txnDetail.getTransferTo().getId())) {
			throw new Exception("From and to accounts are same.");
		}
		List<AccountDTO> myaccounts= accountDO.retrieveMyAccounts(null, null, new AccountDetailFilter()).getContent();
		AccountDTO myAccount;
		if(txnDetail.getTxnType() == TransactionType.IN) {
			myAccount=myaccounts.stream().filter(f->f.getId().equalsIgnoreCase(txnDetail.getTransferTo().getId())).findFirst().orElseThrow(()-> new Exception("Invalid account selected"));
		}else {//TransactionType.OUT || TransactionType.TRANSFER
			myAccount=myaccounts.stream().filter(f->f.getId().equalsIgnoreCase(txnDetail.getTransferFrom().getId())).findFirst().orElseThrow(()-> new Exception("Invalid account selected"));
		}
		txnDetail.setTxnDate(CommonUtils.getSystemDate());
		TransactionDTO transaction=accountDO.createTransaction(txnDetail,myAccount.getProfile());
		return BusinessObjectConverter.toTransactionDetail(transaction, true,null);
	}

	@Override
	public Paginate<AccountDetail> getMyAccounts(Integer pageIndex, Integer pageSize, AccountDetailFilter filter) throws Exception {
		return accountDO.retrieveMyAccounts(pageIndex, pageSize, filter).map(BusinessObjectConverter::toAccountDetail);
	}

}
