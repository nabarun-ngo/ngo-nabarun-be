package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AccountDTO.AccountDTOFilter;

@Service
public interface IAccountInfraService {
	
	AccountDTO getAccountDetails(String id);
	Page<AccountDTO> getAccounts(Integer page, Integer size, AccountDTOFilter filter);
	AccountDTO createAccount(AccountDTO accountDTO);
}
