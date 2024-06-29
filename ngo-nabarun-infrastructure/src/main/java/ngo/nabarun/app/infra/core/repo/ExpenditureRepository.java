package ngo.nabarun.app.infra.core.repo;


import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ngo.nabarun.app.infra.core.entity.Expenditure;

public interface ExpenditureRepository extends MongoRepository<Expenditure,String>{
	

	
	@Query("{deleted : true }")
	List<Expenditure> findAllDeletedExpenditure();
	

	

	
}
