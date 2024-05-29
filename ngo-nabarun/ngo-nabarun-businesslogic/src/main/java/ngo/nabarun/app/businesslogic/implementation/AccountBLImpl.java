package ngo.nabarun.app.businesslogic.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ngo.nabarun.app.businesslogic.IAccountBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AccountDTO.AccountDTOFilter;
import ngo.nabarun.app.infra.dto.TransactionDTO.TransactionDTOFilter;
import ngo.nabarun.app.infra.dto.BankDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UpiDTO;
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
	private BusinessHelper idGenerator;

	@Override
	public Paginate<AccountDetail> getAccounts(Integer page, Integer size, AccountDetailFilter filter) {
		AccountDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new AccountDTOFilter();
			filterDTO.setAccountStatus(filter.getStatus());
			filterDTO.setAccountType(filter.getType());
		}

		Page<AccountDetail> pageDetail = accountInfraService.getAccounts(page, size, filterDTO).map(m -> {
			AccountDetail acc = BusinessObjectConverter.toAccountDetail(m);
			if (!filter.isIncludePaymentDetail()) {
				acc.setUpiDetail(null);
				acc.setBankDetail(null);
			}
			if (!filter.isIncludeBalance()) {
				acc.setCurrentBalance(0.0);
			}
			return acc;
		});
		return new Paginate<AccountDetail>(pageDetail);
	}

	@Override
	public AccountDetail createAccount(AccountDetailCreate accountDetail) throws Exception {
		UserDTO userDTO = userInfraService.getUser(accountDetail.getAccountHolderProfileId(),IdType.ID, false);
		if (userDTO.getStatus() != ProfileStatus.ACTIVE) {
			throw new BusinessException("Account can only be created for an ACTIVE user.");
		}
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setId(idGenerator.generateAccountId());
		accountDTO.setProfile(userDTO);
		accountDTO.setAccountName(userDTO.getName());
		if (accountDetail.getBankDetail() != null) {
			BankDTO bankDTO = new BankDTO();
			bankDTO.setAccountHolderName(accountDetail.getBankDetail().getBankAccountHolderName());
			bankDTO.setAccountNumber(accountDetail.getBankDetail().getBankAccountNumber());
			bankDTO.setAccountType(accountDetail.getBankDetail().getBankAccountType());
			bankDTO.setBankName(accountDetail.getBankDetail().getBankName());
			bankDTO.setBranchName(accountDetail.getBankDetail().getBankBranch());
			bankDTO.setIFSCNumber(accountDetail.getBankDetail().getIFSCNumber());
			accountDTO.setBankDetail(bankDTO);
		}

		if (accountDetail.getUpiDetail() != null) {
			UpiDTO upiDTO = new UpiDTO();
			upiDTO.setMobileNumber(accountDetail.getUpiDetail().getMobileNumber());
			upiDTO.setPayeeName(accountDetail.getUpiDetail().getPayeeName());
			upiDTO.setUpiId(accountDetail.getUpiDetail().getUpiId());
			accountDTO.setUpiDetail(upiDTO);
		}
		accountDTO.setAccountType(accountDetail.getAccountType());
		accountDTO.setAccountStatus(AccountStatus.ACTIVE);
		accountDTO.setActivatedOn(CommonUtils.getSystemDate());
		accountDTO.setOpeningBalance(accountDetail.getOpeningBalance());
		accountDTO = accountInfraService.createAccount(accountDTO);
		/*
		 * Create transaction and update current value if opening balance is > 0
		 */
		if (accountDetail.getOpeningBalance() > 0) {
			TransactionDTO newTxn = new TransactionDTO();
			newTxn.setId(idGenerator.generateTransactionId());
			newTxn.setToAccount(accountDTO);
			newTxn.setTxnAmount(accountDTO.getOpeningBalance());
			newTxn.setTxnDate(accountDTO.getActivatedOn());
			newTxn.setTxnRefId(null);
			newTxn.setTxnRefType(TransactionRefType.NONE);
			newTxn.setTxnStatus(TransactionStatus.SUCCESS);
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setTxnDescription("Initial opening balance for account " + accountDTO.getId());
			newTxn = transactionInfraService.createTransaction(newTxn);
		}
		return BusinessObjectConverter.toAccountDetail(accountDTO);
	}

	@Override
	public AccountDetail updateAccount(String id, AccountDetailUpdate accountDetail) throws Exception {
		return null;
	}

	@Override
	public Paginate<TransactionDetail> getTransactions(String id, int index, int size) {
		TransactionDTOFilter filter = new TransactionDTOFilter();
		filter.setAccountId(id);
		Page<TransactionDTO> transactions = transactionInfraService.getTransactions(index, size, filter);
				//transactionInfraService.getTransactionsForAccount(id, index, size);
		return new Paginate<TransactionDetail>(
				transactions.map(m -> BusinessObjectConverter.toTransactionDetail(m, false)));

	}

}
