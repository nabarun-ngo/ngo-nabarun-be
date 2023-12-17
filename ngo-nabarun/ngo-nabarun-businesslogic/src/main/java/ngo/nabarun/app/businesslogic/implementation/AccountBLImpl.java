package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Page;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.helper.BusinessIdGenerator;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.BankDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UPIDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.service.IAccountInfraService;
import ngo.nabarun.app.infra.service.ITransactionInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
public class AccountBLImpl implements IAccountBL {
	
	@Autowired
	private IAccountInfraService accountInfraService;
	
	@Autowired
	private IUserInfraService userInfraService;
	
	@Autowired
	private ITransactionInfraService transactionInfraService;
	
	@Autowired
	private  BusinessIdGenerator idGenerator;
	
	@Override
	public Page<AccountDetail> getAccounts(Integer page, Integer size) {
		return null;
	}
	


	@Override
	public AccountDetail createAccount(AccountDetailCreate accountDetail) throws Exception {
		UserDTO userDTO=userInfraService.getUserByProfileId(accountDetail.getAccountHolderProfileId(), false);
		if(userDTO.getStatus() != ProfileStatus.ACTIVE) {
			throw new BusinessException("Account can only be created for an ACTIVE user.");
		}
		AccountDTO accountDTO= new AccountDTO();
		accountDTO.setId(idGenerator.generateAccountId());
		accountDTO.setProfile(userDTO);	
		accountDTO.setAccountName(userDTO.getName());
		if(accountDetail.getBankDetail() != null) {
			BankDTO bankDTO = new BankDTO();
			bankDTO.setAccountHolderName(accountDetail.getBankDetail().getBankAccountHolderName());
			bankDTO.setAccountNumber(accountDetail.getBankDetail().getBankAccountNumber());		
			bankDTO.setAccountType(accountDetail.getBankDetail().getBankAccountType());
			bankDTO.setBankName(accountDetail.getBankDetail().getBankName());
			bankDTO.setBranchName(accountDetail.getBankDetail().getBankBranch());
			bankDTO.setIFSCNumber(accountDetail.getBankDetail().getIFSCNumber());
			accountDTO.setBankDetail(bankDTO);
		}
		
		if(accountDetail.getUpiDetail() != null ) {
			UPIDTO upiDTO = new UPIDTO();
			upiDTO.setMobileNumber(accountDetail.getUpiDetail().getMobileNumber());		
			upiDTO.setPayeeName(accountDetail.getUpiDetail().getPayeeName());
			upiDTO.setUpiId(accountDetail.getUpiDetail().getUpiId());
			accountDTO.setUpiDetail(upiDTO);
		}
		accountDTO.setAccountType(accountDetail.getAccountType());
		accountDTO.setAccountStatus(AccountStatus.ACTIVE);
		accountDTO.setActivatedOn(CommonUtils.getSystemDate());
		accountDTO.setOpeningBalance(accountDetail.getOpeningBalance());
		accountDTO=accountInfraService.createAccount(accountDTO);
		/*
		 * Create transaction and update current value if opening balance is > 0
		 */
		if(accountDetail.getOpeningBalance() > 0) {
			TransactionDTO newTxn = new TransactionDTO();
			newTxn.setId(idGenerator.generateTransactionId());
			newTxn.setToAccount(accountDTO);
			newTxn.setTxnAmount(accountDTO.getOpeningBalance());
			newTxn.setTxnDate(accountDTO.getActivatedOn());
			newTxn.setTxnRefId(null);
			newTxn.setTxnRefType(TransactionRefType.NONE);
			newTxn.setTxnStatus(TransactionStatus.SUCCESS);
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setTxnDescription("Initial opening balance for account "+accountDTO.getId());	
			newTxn=transactionInfraService.createTransaction(newTxn);
		}
		return DTOToBusinessObjectConverter.toAccountDetail(accountDTO);
	}

	@Override
	public AccountDetail updateAccount(String id, AccountDetailUpdate accountDetail) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void getTransactions(String id) {
		List<TransactionDTO> trnsactions=transactionInfraService.getTransactionsForAccount(id);
	}

	
	
	
}
