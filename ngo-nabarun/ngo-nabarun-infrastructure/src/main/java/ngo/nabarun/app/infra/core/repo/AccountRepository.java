package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.AccountEntity;

@Repository
public interface AccountRepository extends MongoRepository<AccountEntity,String>{
	
	List<AccountEntity> findByAccountTypeAndAccountStatus(String type,String status);
	
}
