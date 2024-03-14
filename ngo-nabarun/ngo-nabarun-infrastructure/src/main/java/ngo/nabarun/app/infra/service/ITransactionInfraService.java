package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.data.domain.Page;

import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO.TransactionDTOFilter;

public interface ITransactionInfraService {
	TransactionDTO createTransaction(TransactionDTO transaction) throws Exception;	
	List<TransactionDTO> getTransactions(String txnRefNumber,TransactionRefType txnRefType) throws Exception;
	Page<TransactionDTO> getTransactionsForAccount(String id, Integer index, Integer size);
	Page<TransactionDTO> getTransactions(Integer page, Integer size, TransactionDTOFilter filter);

}
