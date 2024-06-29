package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ngo.nabarun.app.infra.core.entity.AccountEntity;

public interface AccountRepository extends MongoRepository<AccountEntity,String>,QuerydslPredicateExecutor<AccountEntity>{
	
	List<AccountEntity> findByAccountTypeAndAccountStatus(String type,String status);
	
}
