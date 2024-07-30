package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;

import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.AccountEntity;
import ngo.nabarun.app.infra.core.entity.QAccountEntity;
import ngo.nabarun.app.infra.core.entity.QTransactionEntity;
import ngo.nabarun.app.infra.core.entity.TransactionEntity;
import ngo.nabarun.app.infra.core.repo.AccountRepository;
import ngo.nabarun.app.infra.core.repo.TransactionRepository;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AccountDTO.AccountDTOFilter;
import ngo.nabarun.app.infra.dto.BankDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO.TransactionDTOFilter;
import ngo.nabarun.app.infra.dto.UpiDTO;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.WhereClause;
import ngo.nabarun.app.infra.service.IAccountInfraService;
import ngo.nabarun.app.infra.service.ITransactionInfraService;

@Service
public class PaymentsInfraServiceImpl implements ITransactionInfraService, IAccountInfraService {

	@Autowired
	private AccountRepository accRepo;

	@Autowired
	private TransactionRepository txnRepo;

	@Override
	public TransactionDTO createTransaction(TransactionDTO transactionDTO) throws Exception {
		// if status is given as success then and update link account
		TransactionEntity txn = new TransactionEntity();
		txn.setId(transactionDTO.getId());
		txn.setComment(transactionDTO.getComment());
		txn.setCreationDate(CommonUtils.getSystemDate());
		txn.setStatus(transactionDTO.getTxnStatus() == null ? null : transactionDTO.getTxnStatus().name());
		txn.setTransactionType(transactionDTO.getTxnType() == null ? null : transactionDTO.getTxnType().name());
		txn.setTransactionAmt(transactionDTO.getTxnAmount());
		txn.setTransactionDate(transactionDTO.getTxnDate());
		txn.setTransactionRefId(transactionDTO.getTxnRefId());
		txn.setTransactionRefType(
				transactionDTO.getTxnRefType() == null ? null : transactionDTO.getTxnRefType().name());
		txn.setTransactionDescription(transactionDTO.getTxnDescription());
		;

		AccountEntity srcAccount = null;
		AccountEntity destAccount = null;
		if (transactionDTO.getTxnStatus() == TransactionStatus.SUCCESS) {
			if (transactionDTO.getTxnType() == TransactionType.IN) {
				/*
				 * updating current balance
				 */
				destAccount = accRepo.findById(transactionDTO.getToAccount().getId()).orElseThrow();
				destAccount.setCurrentBalance(destAccount.getCurrentBalance() + transactionDTO.getTxnAmount());
				destAccount = accRepo.save(destAccount);
				txn.setToAccBalAfterTxn(destAccount.getCurrentBalance());
				txn.setToAccount(transactionDTO.getToAccount().getId());
			} else if (transactionDTO.getTxnType() == TransactionType.OUT) {
				srcAccount = accRepo.findById(transactionDTO.getFromAccount().getId()).orElseThrow();
				srcAccount.setCurrentBalance(srcAccount.getCurrentBalance() - transactionDTO.getTxnAmount());
				srcAccount = accRepo.save(srcAccount);
				txn.setFromAccBalAfterTxn(srcAccount.getCurrentBalance());
				txn.setFromAccount(transactionDTO.getFromAccount().getId());
			} else if (transactionDTO.getTxnType() == TransactionType.TRANSFER) {
				srcAccount = accRepo.findById(transactionDTO.getFromAccount().getId()).orElseThrow();
				srcAccount.setCurrentBalance(srcAccount.getCurrentBalance() - transactionDTO.getTxnAmount());
				srcAccount = accRepo.save(srcAccount);
				txn.setFromAccBalAfterTxn(srcAccount.getCurrentBalance());
				txn.setFromAccount(transactionDTO.getFromAccount().getId());

				destAccount = accRepo.findById(transactionDTO.getToAccount().getId()).orElseThrow();
				destAccount.setCurrentBalance(destAccount.getCurrentBalance() + transactionDTO.getTxnAmount());
				destAccount = accRepo.save(destAccount);
				txn.setToAccBalAfterTxn(destAccount.getCurrentBalance());
				txn.setToAccount(transactionDTO.getToAccount().getId());
			}
		}
		
		if (transactionDTO.getCreatedBy() != null) {
			txn.setCreatedById(transactionDTO.getCreatedBy().getProfileId());
			txn.setCreatedByName(transactionDTO.getCreatedBy().getName());
			txn.setCreatedByEmail(transactionDTO.getCreatedBy().getEmail());
		}
		
		
		return InfraDTOHelper.convertToTransactionDTO(txnRepo.save(txn), srcAccount, destAccount);
	}

	@Override
	public List<TransactionDTO> getTransactions(String txnRefNumber, TransactionRefType txnRefType) throws Exception {
		List<TransactionEntity> txnList = txnRepo.findByTransactionRefIdAndTransactionRefType(txnRefNumber,
				txnRefType.name());
		return txnList.stream().map(m -> InfraDTOHelper.convertToTransactionDTO(m, null, null)).toList();
	}

	@Override
	public AccountDTO getAccountDetails(String id) {
		AccountEntity accountInfo = accRepo.findById(id).orElseThrow();
		return InfraDTOHelper.convertToAccountDTO(accountInfo, null);
	}

	@Override
	public Page<AccountDTO> getAccounts(Integer page, Integer size, AccountDTOFilter filter) {
		Page<AccountEntity> accountPage = null;
		if (filter != null) {

			/*
			 * Query building and filter logic
			 */
			QAccountEntity qAccount = QAccountEntity.accountEntity;
			BooleanBuilder query = WhereClause.builder()
					.optionalAnd(filter.getAccountId() != null, () -> qAccount.id.eq(filter.getAccountId()))
					.optionalAnd(filter.getProfileId() != null, () -> qAccount.profile.eq(filter.getProfileId()))
					.optionalAnd(filter.getAccountStatus() != null,
							() -> qAccount.accountStatus
									.in(filter.getAccountStatus().stream().map(m -> m.name()).toList()))
					.optionalAnd(filter.getAccountType() != null,
							() -> qAccount.accountType.in(filter.getAccountType().stream().map(m -> m.name()).toList()))
					.build();
			if (page == null || size == null) {
				List<AccountEntity> result = new ArrayList<>();
				accRepo.findAll(query).iterator().forEachRemaining(result::add);
				accountPage = new PageImpl<>(result);
			} else {
				accountPage = accRepo.findAll(query, PageRequest.of(page, size));
			}
		} else if (page != null && size != null) {
			accountPage = accRepo.findAll(PageRequest.of(page, size));
		} else {
			accountPage = new PageImpl<>(accRepo.findAll());
		}
		return accountPage.map(m -> InfraDTOHelper.convertToAccountDTO(m, null));
	}

	@Override
	public AccountDTO createAccount(AccountDTO accountDTO) {
		AccountEntity entity = new AccountEntity();
		entity.setId(accountDTO.getId());
		entity.setAccountName(accountDTO.getAccountName());
		entity.setAccountStatus(accountDTO.getAccountStatus() == null ? null : accountDTO.getAccountStatus().name());
		entity.setAccountType(accountDTO.getAccountType() == null ? null : accountDTO.getAccountType().name());
		entity.setActivatedOn(accountDTO.getActivatedOn());
		entity.setCreatedOn(CommonUtils.getSystemDate());
		entity.setOpeningBalance(accountDTO.getOpeningBalance());
		entity.setCurrentBalance(0.0);
		entity.setProfile(accountDTO.getProfile().getProfileId());
		if (accountDTO.getBankDetail() != null) {
			BankDTO bankDetail = accountDTO.getBankDetail();
			entity.setBankAccountHolderName(bankDetail.getAccountHolderName());
			entity.setBankAccountNumber(bankDetail.getAccountNumber());
			entity.setBankAccountType(bankDetail.getAccountType());
			entity.setBankBranchName(bankDetail.getBranchName());
			entity.setBankIFSCNumber(bankDetail.getIFSCNumber());
			entity.setBankName(bankDetail.getBankName());
		}

		if (accountDTO.getUpiDetail() != null) {
			UpiDTO upiDetail = accountDTO.getUpiDetail();
			entity.setUpiId(upiDetail.getUpiId());
			entity.setUpiMobileNumber(upiDetail.getMobileNumber());
			entity.setUpiPayeeName(upiDetail.getPayeeName());
		}
		
		if (accountDTO.getCreatedBy() != null) {
			entity.setCreatedById(accountDTO.getCreatedBy().getProfileId());
			entity.setCreatedByName(accountDTO.getCreatedBy().getName());
			entity.setCreatedByEmail(accountDTO.getCreatedBy().getEmail());
		}

		return InfraDTOHelper.convertToAccountDTO(accRepo.save(entity), null);
	}

//	@Override
//	@Deprecated
//	public Page<TransactionDTO> getTransactionsForAccount(String id, Integer page, Integer size) {
//		Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
//		Page<TransactionEntity> transactions = null;
//		if (page != null && size != null) {
//			transactions = txnRepo.findByFromAccountOrToAccount(id, id, PageRequest.of(page, size, sort));
//		} else {
//			transactions = new PageImpl<>(txnRepo.findByFromAccountOrToAccount(id, id, sort));
//		}
//		return transactions.map(m -> InfraDTOHelper.convertToTransactionDTO(m, null, null));
//	}

	@Override
	public Page<TransactionDTO> getTransactions(Integer page, Integer size, TransactionDTOFilter filter) {
		Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
		Page<TransactionEntity> transactions = null;
		if (filter != null) {

			/*
			 * Query building and filter logic
			 */
			QTransactionEntity qTxn = QTransactionEntity.transactionEntity;
			BooleanBuilder query = WhereClause.builder()
					.optionalAnd(filter.getId() != null, () -> qTxn.id.eq(filter.getId()))
					.optionalAnd(filter.getTxnStatus() != null,
							() -> qTxn.status.in(filter.getTxnStatus().stream().map(m -> m.name()).toList()))
					.optionalAnd(filter.getTxnType() != null,
							() -> qTxn.transactionType.in(filter.getTxnType().stream().map(m -> m.name()).toList()))
					.optionalAnd(filter.getTxnRefId() != null, () -> qTxn.transactionRefId.eq(filter.getTxnRefId()))
					.optionalAnd(filter.getTxnRefType() != null,
							() -> qTxn.transactionRefType.eq(filter.getTxnRefType().name()))
					.optionalAnd(filter.getAccountId() != null,
							() -> qTxn.fromAccount.eq(filter.getAccountId())
									.or(qTxn.toAccount.eq(filter.getAccountId())))

					.optionalAnd(filter.getFromDate() != null && filter.getToDate() != null,
							() -> qTxn.creationDate.between(filter.getFromDate(), filter.getToDate()))
					.build();
			if (page == null || size == null) {
				List<TransactionEntity> result = new ArrayList<>();
				txnRepo.findAll(query, sort).iterator().forEachRemaining(result::add);
				transactions = new PageImpl<>(result);
			} else {
				transactions = txnRepo.findAll(query, PageRequest.of(page, size, sort));
			}
		} else if (page != null && size != null) {
			transactions = txnRepo.findAll(PageRequest.of(page, size, sort));
		} else {
			transactions = new PageImpl<>(txnRepo.findAll(sort));
		}
		return transactions.map(m -> InfraDTOHelper.convertToTransactionDTO(m, null, null));
	}

	@Override
	public AccountDTO updateAccount(String id, AccountDTO accountUpdate) {
		AccountEntity account = accRepo.findById(id).orElseThrow();

		AccountEntity updatedAccount = new AccountEntity();
		updatedAccount.setAccountStatus(
				accountUpdate.getAccountStatus() == null ? null : accountUpdate.getAccountStatus().name());
		
		if(accountUpdate.getBankDetail() != null) {
			updatedAccount.setBankAccountHolderName(accountUpdate.getBankDetail().getAccountHolderName());
			updatedAccount.setBankAccountNumber(accountUpdate.getBankDetail().getAccountNumber());
			updatedAccount.setBankAccountType(accountUpdate.getBankDetail().getAccountType());
			updatedAccount.setBankBranchName(accountUpdate.getBankDetail().getBranchName());
			updatedAccount.setBankIFSCNumber(accountUpdate.getBankDetail().getIFSCNumber());
			updatedAccount.setBankName(accountUpdate.getBankDetail().getBankName());
		}
		
		if(accountUpdate.getUpiDetail() != null) {
			updatedAccount.setUpiId(accountUpdate.getUpiDetail().getUpiId());
			updatedAccount.setUpiMobileNumber(accountUpdate.getUpiDetail().getMobileNumber());
			updatedAccount.setUpiPayeeName(accountUpdate.getUpiDetail().getPayeeName());
		}
		
		
		CommonUtils.copyNonNullProperties(updatedAccount, account);
		account = accRepo.save(account);
		return InfraDTOHelper.convertToAccountDTO(account, null);
	}

	@Override
	public TransactionDTO updateTransaction(String id, TransactionDTO oldTxn) {
		TransactionEntity transaction = txnRepo.findById(id).orElseThrow();

		TransactionEntity updatedtransaction = new TransactionEntity();
		updatedtransaction.setStatus(oldTxn.getTxnStatus() == null ? null : oldTxn.getTxnStatus().name());
		
		if (oldTxn.isTxnReverted()) {
			updatedtransaction.setRevertedTransaction(oldTxn.isTxnReverted());
			updatedtransaction.setRevertedById(oldTxn.getRevertedBy().getProfileId());
			updatedtransaction.setRevertedByName(oldTxn.getRevertedBy().getName());
			updatedtransaction.setRevertedByEmail(oldTxn.getRevertedBy().getEmail());
			updatedtransaction.setRevertRefTxnId(oldTxn.getRevertRefTxnId());
		}

		CommonUtils.copyNonNullProperties(updatedtransaction, transaction);
		transaction = txnRepo.save(transaction);
		return InfraDTOHelper.convertToTransactionDTO(transaction, null, null);
	}

	@Override
	public TransactionDTO getTransaction(String id) {
		TransactionEntity transaction = txnRepo.findById(id).orElseThrow();	;
		return InfraDTOHelper.convertToTransactionDTO(transaction, null, null);
	}
}
