package ngo.nabarun.app.infra.core.repo;


import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ngo.nabarun.app.infra.core.entity.TransactionEntity;


public interface TransactionRepository extends MongoRepository<TransactionEntity,String>,QuerydslPredicateExecutor<TransactionEntity>{

	List<TransactionEntity> findByTransactionRefIdAndTransactionRefType(String txnRefNumber, String txnRefType);
	List<TransactionEntity> findByFromAccountOrToAccount(String fromAccount, String toAccount, Sort sort);
	Page<TransactionEntity> findByFromAccountOrToAccount(String fromAccount, String toAccount,Pageable page);
}
