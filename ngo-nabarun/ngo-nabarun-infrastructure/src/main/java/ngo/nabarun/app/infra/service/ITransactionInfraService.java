package ngo.nabarun.app.infra.service;

import java.util.List;

import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.infra.dto.TransactionDTO;

public interface ITransactionInfraService {
	TransactionDTO createTransaction(TransactionDTO transaction) throws Exception;	
	List<TransactionDTO> getTransactions(String txnRefNumber,TransactionRefType txnRefType) throws Exception;
	List<TransactionDTO> getTransactionsForAccount(String id);

}
