package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AccountDTO.AccountDTOFilter;
import ngo.nabarun.app.infra.dto.ExpenseDTO;
import ngo.nabarun.app.infra.dto.ExpenseDTO.ExpenseDTOFilter;
import ngo.nabarun.app.infra.dto.ExpenseDTO.ExpenseItemDTO;

@Service
public interface IAccountInfraService {
	
	AccountDTO getAccountDetails(String id);
	Page<AccountDTO> getAccounts(Integer page, Integer size, AccountDTOFilter filter);
	AccountDTO createAccount(AccountDTO accountDTO);
	AccountDTO updateAccount(String id, AccountDTO accountUpdate);
	void deleteAccount(String id);
	ExpenseDTO addOrUpdateExpense(ExpenseDTO expenseDTO);
	Page<ExpenseDTO> getExpenses(Integer index, Integer size, ExpenseDTOFilter expenseDTOFilter);
	ExpenseDTO getExpense(String id);
	ExpenseItemDTO addOrUpdateExpenseItem(String expenseId, ExpenseItemDTO expenseItemDTO);
}
