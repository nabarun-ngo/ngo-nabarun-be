package ngo.nabarun.app.infra.core.repo;


import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.TransactionEntity;


@Repository
public interface TransactionRepository extends MongoRepository<TransactionEntity,String>{

	List<TransactionEntity> findByTransactionRefIdAndTransactionRefType(String txnRefNumber, String txnRefType);
	List<TransactionEntity> findByFromAccountOrToAccount(String fromAccount, String toAccount);
//{ $or: [ { toAccount: "NACC202312134269A4"}, { fromAccount: "NACC202312134269A4" } ] }
}
