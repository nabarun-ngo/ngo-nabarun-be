package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.Account;

@Repository
public interface AccountRepository extends MongoRepository<Account,String>{
	
	List<Account> findByAccountTypeAndAccountStatus(String type,String status);
	
}
