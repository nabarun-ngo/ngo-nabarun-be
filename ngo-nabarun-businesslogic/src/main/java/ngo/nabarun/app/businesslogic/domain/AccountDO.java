package ngo.nabarun.app.businesslogic.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail.AccountDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail.ExpenseDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.ExpenseDetail.ExpenseItemDetail;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail.TransactionDetailFilter;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.AccountType;
import ngo.nabarun.app.common.enums.ExpenseStatus;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.common.util.SecurityUtils.AuthenticatedUser;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.BankDTO;
import ngo.nabarun.app.infra.dto.ExpenseDTO;
import ngo.nabarun.app.infra.dto.ExpenseDTO.ExpenseDTOFilter;
import ngo.nabarun.app.infra.dto.ExpenseDTO.ExpenseItemDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UpiDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.AccountDTO.AccountDTOFilter;
import ngo.nabarun.app.infra.dto.TransactionDTO.TransactionDTOFilter;
import ngo.nabarun.app.infra.service.IAccountInfraService;
import ngo.nabarun.app.infra.service.ITransactionInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Component
public class AccountDO extends CommonDO {

	@Autowired
	private ITransactionInfraService transactionInfraService;

	@Autowired
	protected IAccountInfraService accountInfraService;

	@Autowired
	protected IUserInfraService userInfraService;

	/**
	 * 
	 * @param page
	 * @param size
	 * @param filter
	 * @return
	 */
	public Paginate<AccountDTO> retrieveAccounts(Integer page, Integer size, AccountDetailFilter filter) {
		AccountDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new AccountDTOFilter();
			filterDTO.setAccountStatus(filter.getStatus());
			filterDTO.setAccountType(filter.getType());
			filterDTO.setProfileId(filter.getAccountHolderId());
			filterDTO.setAccountId(filter.getAccountId());
		}

		Page<AccountDTO> pageDetail = accountInfraService.getAccounts(page, size, filterDTO).map(acc -> {
			if (!filter.isIncludePaymentDetail()) {
				acc.setUpiDetail(null);
				acc.setBankDetail(null);
			}
			if (!filter.isIncludeBalance()) {
				acc.setCurrentBalance(0.0);
			}
			return acc;
		});
		return new Paginate<AccountDTO>(pageDetail);
	}

	public AccountDTO retrieveAccount(String id) {
		return accountInfraService.getAccountDetails(id);
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public List<AccountDTO> retrievePayableAccounts(AccountType... type) {
		AccountDTOFilter filter = new AccountDTOFilter();
		filter.setAccountStatus(List.of(AccountStatus.ACTIVE));
		filter.setAccountType(List.of(type));
		List<AccountDTO> accounts = accountInfraService.getAccounts(null, null, filter).getContent();
		return accounts;
	}

	/**
	 * 
	 * @param accountDetail
	 * @param openingBal
	 * @return
	 * @throws Exception
	 */
	public AccountDTO createAccount(AccountDetail accountDetail, Double openingBal, String logged_in_user)
			throws Exception {
		UserDTO userDTO = userInfraService.getUser(accountDetail.getAccountHolder().getId(), IdType.ID, false);
		if (userDTO.getStatus() != ProfileStatus.ACTIVE) {
			throw new BusinessException("Account can only be created for an ACTIVE user.");
		}
		UserDTO auth_user = userInfraService.getUser(logged_in_user, IdType.AUTH_USER_ID, false);

		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setId(generateAccountId());
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
		accountDTO.setOpeningBalance(openingBal);
		accountDTO.setCreatedBy(auth_user);
		accountDTO = accountInfraService.createAccount(accountDTO);
		/*
		 * Create transaction and update current value if opening balance is > 0
		 */
		if (accountDTO.getOpeningBalance() != null && accountDTO.getOpeningBalance() > 0) {
			TransactionDetail newTxn = new TransactionDetail();
			newTxn.setTxnAmount(openingBal);
			newTxn.setTxnAmount(accountDTO.getOpeningBalance());
			newTxn.setTxnDate(accountDTO.getActivatedOn());
			newTxn.setTxnRefId(null);
			newTxn.setTxnRefType(TransactionRefType.NONE);
			newTxn.setTxnStatus(TransactionStatus.SUCCESS);
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setTxnDescription("Initial opening balance for account " + accountDTO.getId());
			AccountDetail toAccount = BusinessObjectConverter.toAccountDetail(accountDTO);
			newTxn.setTransferTo(toAccount);
			createTransaction(newTxn, auth_user);
		}
		return accountDTO;
	}

	/**
	 * 
	 * @param id
	 * @param index
	 * @param size
	 * @param filter2
	 * @return
	 */
	public Paginate<TransactionDTO> retrieveAccountTransactions(String id, Integer index, Integer size,
			TransactionDetailFilter filter) {
		TransactionDTOFilter filterDTO = new TransactionDTOFilter();
		filterDTO.setAccountId(id);
		filterDTO.setTxnId(filter.getTxnId());
		filterDTO.setFromDate(filter.getStartDate());
		filterDTO.setToDate(filter.getEndDate());
		filterDTO.setTxnRefType(filter.getTxnRefType());
		filterDTO.setTxnStatus(filter.getTxnStatus());
		filterDTO.setTxnType(filter.getTxnType());
		Page<TransactionDTO> transactions = transactionInfraService.getTransactions(index, size, filterDTO);
		return new Paginate<TransactionDTO>(transactions);
	}

	/**
	 * 
	 * @param refId
	 * @param refType
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public List<TransactionDTO> retrieveTransactions(String refId, TransactionRefType refType, TransactionStatus status)
			throws Exception {
		List<TransactionDTO> allTxns = transactionInfraService.getTransactions(refId, refType);
		if (status != null) {
			return allTxns.stream().filter(f -> f.getTxnStatus() == status).collect(Collectors.toList());
		}
		return allTxns;
	}

	/**
	 * 
	 * @param transaction
	 * @return
	 * @throws Exception
	 */
	public TransactionDTO createTransaction(TransactionDTO transaction, UserDTO auth_user) throws Exception {
		/*
		 * Checking for any existing transactions created against the ref id if one or
		 * more transaction exists then checking if any of the transaction status is
		 * success or not create a transaction with success status if none of then is
		 * success
		 */
		List<TransactionDTO> successTxn = new ArrayList<>();
		if (transaction.getTxnRefId() != null) {
			List<TransactionDTO> nonRevertedTxn = retrieveTransactions(transaction.getTxnRefId(),
					transaction.getTxnRefType(), TransactionStatus.SUCCESS).stream().filter(f -> !f.isTxnReverted())
					.collect(Collectors.toList());
			successTxn.addAll(nonRevertedTxn);
		}

		if (successTxn.isEmpty()) {
			transaction.setId(generateTransactionId());
			transaction.setCreatedBy(auth_user);
			TransactionDTO newTxn = transactionInfraService.createTransaction(transaction);

			if (newTxn.getFromAccount() != null && newTxn.getFromAccount().getProfile() != null
					&& newTxn.getFromAccount().getProfile().getUserId() != null) {
				String fromAccountUserId = newTxn.getFromAccount().getProfile().getUserId();
				updateAndSendDashboardCounts(fromAccountUserId, data -> {
					Map<String, String> map = new HashMap<>();
					map.put(BusinessConstants.attr_DB_accountBalance, String.valueOf(newTxn.getFromAccBalAfterTxn()));
					return map;
				});
			}

			if (newTxn.getToAccount() != null && newTxn.getToAccount().getProfile() != null
					&& newTxn.getToAccount().getProfile().getUserId() != null) {
				String toAccountUserId = newTxn.getToAccount().getProfile().getUserId();
				updateAndSendDashboardCounts(toAccountUserId, data -> {
					Map<String, String> map = new HashMap<>();
					map.put(BusinessConstants.attr_DB_accountBalance, String.valueOf(newTxn.getToAccBalAfterTxn()));
					return map;
				});
			}
			return newTxn;
		}
		return successTxn.get(0);
	}

	public TransactionDTO createTransaction(TransactionDetail transaction, UserDTO auth_user) throws Exception {
		TransactionDTO newTxn = new TransactionDTO();
		newTxn.setTxnAmount(transaction.getTxnAmount());
		newTxn.setTxnDate(transaction.getTxnDate());
		newTxn.setTxnRefId(transaction.getTxnRefId());
		newTxn.setTxnRefType(transaction.getTxnRefType());
		newTxn.setTxnStatus(transaction.getTxnStatus());
		newTxn.setTxnType(transaction.getTxnType());
		newTxn.setCreatedBy(auth_user);
		newTxn.setTxnDescription(transaction.getTxnDescription());
		if (transaction.getTxnType() == TransactionType.IN) {
			AccountDTO destAccDTO = new AccountDTO();
			destAccDTO.setId(transaction.getTransferTo().getId());
			destAccDTO.setAccountName(transaction.getTransferTo().getAccountHolderName());
			newTxn.setToAccount(destAccDTO);
		} else if (transaction.getTxnType() == TransactionType.OUT) {
			AccountDTO srcAccDTO = new AccountDTO();
			srcAccDTO.setId(transaction.getTransferFrom().getId());
			srcAccDTO.setAccountName(transaction.getTransferFrom().getAccountHolderName());
			newTxn.setFromAccount(srcAccDTO);
		} else {
			AccountDTO destAccDTO = new AccountDTO();
			destAccDTO.setId(transaction.getTransferTo().getId());
			destAccDTO.setAccountName(transaction.getTransferTo().getAccountHolderName());
			newTxn.setToAccount(destAccDTO);
			AccountDTO srcAccDTO = new AccountDTO();
			srcAccDTO.setId(transaction.getTransferFrom().getId());
			srcAccDTO.setAccountName(transaction.getTransferFrom().getAccountHolderName());
			newTxn.setFromAccount(srcAccDTO);
		}
		return createTransaction(newTxn, auth_user);
	}

	public void revertTransaction(String refId, TransactionRefType refType, TransactionStatus status, UserDTO auth_user)
			throws Exception {
		List<TransactionDTO> transactions = retrieveTransactions(refId, refType, status);

		for (TransactionDTO transaction : transactions) {
			revertTransaction(transaction, auth_user);
		}
	}

	public TransactionDTO retrieveTransaction(String id) throws Exception {
		return transactionInfraService.getTransaction(id);
	}

	/**
	 * This will return latest transaction instance
	 * 
	 * @return
	 */
	public TransactionDTO revertTransaction(TransactionDTO transaction, UserDTO auth_user) throws Exception {
		if (transaction.getTxnStatus() != TransactionStatus.SUCCESS && transaction.isTxnReverted()) {
			throw new Exception("Transaction cannot be reverted as status is not successful or already reverted");
		}
		TransactionDTO newTxn = new TransactionDTO();
		newTxn.setTxnAmount(transaction.getTxnAmount());
		newTxn.setTxnDate(transaction.getTxnDate());
		newTxn.setTxnRefId(transaction.getTxnRefId());
		newTxn.setTxnRefType(transaction.getTxnRefType());
		newTxn.setTxnStatus(TransactionStatus.SUCCESS);
		newTxn.setTxnDescription("Reverting last transaction " + transaction.getId());

		if (transaction.getTxnType() == TransactionType.IN) {
			newTxn.setTxnType(TransactionType.OUT);
			newTxn.setFromAccount(transaction.getToAccount());
		} else if (transaction.getTxnType() == TransactionType.OUT) {
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setToAccount(transaction.getFromAccount());
		} else {
			newTxn.setTxnType(TransactionType.OUT);
			newTxn.setFromAccount(transaction.getToAccount());
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setToAccount(transaction.getFromAccount());
		}
		TransactionDTO revertedTxn = createTransaction(newTxn, auth_user);
		TransactionDTO oldTxn = new TransactionDTO();
		oldTxn.setRevertedBy(auth_user);
		oldTxn.setTxnReverted(true);
		oldTxn.setRevertRefTxnId(revertedTxn.getRevertRefTxnId());
		transactionInfraService.updateTransaction(transaction.getId(), oldTxn);
		return revertedTxn;
	}

	public Paginate<AccountDTO> retrieveMyAccounts(Integer pageIndex, Integer pageSize, AccountDetailFilter filter)
			throws Exception {
		UserDTO me = userInfraService.getUser(SecurityUtils.getAuthUserId(), IdType.AUTH_USER_ID, false);
		filter.setAccountHolderId(me.getProfileId());
		return retrieveAccounts(pageIndex, pageSize, filter);
	}

	public AccountDTO updateAccount(String id, AccountDetail accountDetail) {
		AccountDTO accountUpdate = new AccountDTO();
		accountUpdate.setAccountStatus(accountDetail.getAccountStatus());
		if (accountDetail.getBankDetail() != null) {
			BankDTO bankDTO = new BankDTO();
			bankDTO.setAccountHolderName(accountDetail.getBankDetail().getBankAccountHolderName());
			bankDTO.setAccountNumber(accountDetail.getBankDetail().getBankAccountNumber());
			bankDTO.setAccountType(accountDetail.getBankDetail().getBankAccountType());
			bankDTO.setBankName(accountDetail.getBankDetail().getBankName());
			bankDTO.setBranchName(accountDetail.getBankDetail().getBankBranch());
			bankDTO.setIFSCNumber(accountDetail.getBankDetail().getIFSCNumber());
			accountUpdate.setBankDetail(bankDTO);
		}
		if (accountDetail.getUpiDetail() != null) {
			UpiDTO upiDTO = new UpiDTO();
			upiDTO.setMobileNumber(accountDetail.getUpiDetail().getMobileNumber());
			upiDTO.setPayeeName(accountDetail.getUpiDetail().getPayeeName());
			upiDTO.setUpiId(accountDetail.getUpiDetail().getUpiId());
			accountUpdate.setUpiDetail(upiDTO);
		}
		return accountInfraService.updateAccount(id, accountUpdate);

	}

	public ExpenseDTO createExpense(ExpenseDetail expense) throws Exception {
		ExpenseDTO expenseDTO = new ExpenseDTO();
		expenseDTO.setDescription(expense.getDescription());
		expenseDTO.setName(expense.getName());
		expenseDTO.setRefType(expense.getExpenseRefType());
		expenseDTO.setRefId(expense.getExpenseRefId());
		expenseDTO.setExpenseDate(expense.getExpenseDate());
		
		AuthenticatedUser auth_user = SecurityUtils.getAuthUser();
		UserDTO loggedInUser = new UserDTO();
		loggedInUser.setName(auth_user.getName());
		loggedInUser.setProfileId(auth_user.getId());
		loggedInUser.setUserId(auth_user.getUserId());
		expenseDTO.setCreatedBy(loggedInUser);
		expenseDTO.setStatus(ExpenseStatus.SUBMITTED);
//		AccountDTO accountDTO = accountInfraService
//				.getAccountDetails(expense.getAccount().getId());
//		expenseDTO.setAccount(accountDTO);
		expenseDTO.setId(generateExpenseId());

		expenseDTO= accountInfraService.addOrUpdateExpense(expenseDTO);
		if(expense.getExpenseItems() != null) {
			for(ExpenseItemDetail expenseItem:expense.getExpenseItems()) {
				ExpenseItemDTO expenseItemDTO = new ExpenseItemDTO();
				expenseItemDTO.setAmount(expenseItem.getAmount());
				expenseItemDTO.setCreatedBy(loggedInUser);
				expenseItemDTO.setCreatedOn(CommonUtils.getSystemDate());
				expenseItemDTO.setDescription(expenseItem.getDescription());
				expenseItemDTO.setItemName(expenseItem.getItemName());
			}
		}
		return expenseDTO;
	}

	public Paginate<ExpenseDTO> getExpenses(Integer index, Integer size, ExpenseDetailFilter filter) {
		ExpenseDTOFilter expenseDTOFilter = new ExpenseDTOFilter();
		Page<ExpenseDTO> expensePage = accountInfraService.getExpenses(index, size, expenseDTOFilter);
		return new Paginate<ExpenseDTO>(expensePage);
	}

	public ExpenseDTO updateExpense(String id, ExpenseDetail expense) throws Exception {
		ExpenseDTO expenseDetail = accountInfraService.getExpense(id);
		if (expenseDetail.isFinalized()) {
			throw new BusinessException("No updates allowed on Final expense.");
		}
		ExpenseDTO expenseDTO = new ExpenseDTO();
		expenseDTO.setId(expenseDetail.getId());
		expenseDTO.setName(expense.getName());
		expenseDTO.setDescription(expense.getDescription());

		AuthenticatedUser auth_user = SecurityUtils.getAuthUser();
		UserDTO loggedInUser = new UserDTO();
		loggedInUser.setName(auth_user.getName());
		loggedInUser.setProfileId(auth_user.getId());
		loggedInUser.setUserId(auth_user.getUserId());

		expenseDTO.setFinalized(expense.isFinalized());
		if (expense.isFinalized()) {
			expenseDTO.setFinalizedBy(loggedInUser);
		}

		if (expense.getExpenseItems() != null) {
			expenseDTO.setExpenseItems(new ArrayList<>());
			for (ExpenseItemDetail expenseItem : expense.getExpenseItems()) {
				ExpenseItemDTO expenseItemDTOC = expenseDetail.getExpenseItems().stream()
						.filter(f -> f.getId().equals(expenseItem.getId())).findFirst().orElse(null);

				if (expenseItemDTOC != null && expenseItemDTOC.getStatus() != ExpenseStatus.PAID) {
					ExpenseItemDTO expenseItemDTO = new ExpenseItemDTO();
					AccountDTO accountDTO = accountInfraService
							.getAccountDetails(expenseItem.getExpenseAccount().getId());
					expenseItemDTO.setAccount(accountDTO);

					expenseItemDTO.setAmount(expenseItem.getAmount());
					expenseItemDTO.setConfirmedBy(loggedInUser);
					expenseItemDTO.setDescription(expenseItem.getDescription());
					expenseItemDTO.setItemName(expenseItem.getItemName());
					expenseItemDTO.setRemove(expenseItem.isRemove());
					expenseItemDTO.setStatus(expenseItem.getStatus());

					if (expenseItem.getStatus() == ExpenseStatus.PAID) {
						TransactionDTO newTxn = new TransactionDTO();
						newTxn.setTxnAmount(expenseItem.getAmount());
						newTxn.setTxnDate(CommonUtils.getSystemDate());
						newTxn.setTxnRefId(expenseDetail.getId());
						newTxn.setTxnRefType(TransactionRefType.EXPENSE);
						newTxn.setTxnStatus(TransactionStatus.SUCCESS);
						newTxn.setTxnType(TransactionType.OUT);
						newTxn.setFromAccount(expenseItemDTO.getAccount());
						newTxn.setCreatedBy(loggedInUser);
						newTxn.setTxnDescription("Txn Expense : " + expenseDetail.getName() + "("
								+ expenseDetail.getId() + ") for item " + expenseItem.getItemName());
						newTxn.setComment("Amount PAID.");
						newTxn = createTransaction(newTxn, loggedInUser);
						expenseItemDTO.setTxnNumber(newTxn.getId());
						Double lastAmount = expenseDetail.getFinalAmount() == null ? 0.0
								: expenseDetail.getFinalAmount();
						expenseDTO.setFinalAmount(lastAmount + newTxn.getTxnAmount());
					}
					expenseItemDTO = accountInfraService.addOrUpdateExpenseItem(expenseDetail.getId(), expenseItemDTO);
					expenseDTO.getExpenseItems().add(expenseItemDTO);
				}
			}
		}
		ExpenseDTO updatedExpense = accountInfraService.addOrUpdateExpense(expenseDTO);
		updatedExpense.setExpenseItems(expenseDTO.getExpenseItems());
		return updatedExpense;
	}

	public ExpenseItemDTO createExpenseItem(String id, ExpenseItemDetail expenseItem) throws Exception {
		ExpenseDTO expenseDetail = accountInfraService.getExpense(id);
		if (expenseDetail.isFinalized()) {
			throw new BusinessException("No expense item can be added on Final expense.");
		}
		ExpenseItemDTO expenseItemDTO = new ExpenseItemDTO();
		expenseItemDTO.setAmount(expenseItem.getAmount());
		AuthenticatedUser auth_user = SecurityUtils.getAuthUser();
		UserDTO loggedInUser = new UserDTO();
		loggedInUser.setName(auth_user.getName());
		loggedInUser.setProfileId(auth_user.getId());
		loggedInUser.setUserId(auth_user.getUserId());
		expenseItemDTO.setCreatedBy(loggedInUser);
		
		expenseItemDTO.setCreatedOn(CommonUtils.getSystemDate());
		expenseItemDTO.setDate(expenseItem.getExpenseDate());
		expenseItemDTO.setDescription(expenseItem.getDescription());
		expenseItemDTO.setItemName(expenseItem.getItemName());

		//expenseItemDTO.setId(id);
		expenseItemDTO.setStatus(ExpenseStatus.SUBMITTED);
		return accountInfraService.addOrUpdateExpenseItem(expenseDetail.getId(), expenseItemDTO);
	}

}
