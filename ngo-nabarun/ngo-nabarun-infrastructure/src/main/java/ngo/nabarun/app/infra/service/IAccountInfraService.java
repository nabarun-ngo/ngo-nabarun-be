package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.AccountDTO;

@Service
public interface IAccountInfraService {
	AccountDTO getAccountDetails(String id);
	List<AccountDTO> getAccounts();
	AccountDTO createAccount(AccountDTO accountDTO);
}
