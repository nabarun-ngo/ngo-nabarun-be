package ngo.nabarun.app.infra.core.repo;


import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.Transaction;


@Repository
public interface TransactionRepository extends MongoRepository<Transaction,String>{
	Optional<Transaction> findByTransactionRef(@Param("transactionRef") String transactionRef);

}
