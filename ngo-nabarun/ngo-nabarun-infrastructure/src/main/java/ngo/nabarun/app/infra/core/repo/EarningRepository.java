package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ngo.nabarun.app.infra.core.entity.Earning;

public interface EarningRepository extends MongoRepository<Earning,String>{

	
	@Query("{deleted : true }")
	List<Earning> findAllDeletedEarning();
}
