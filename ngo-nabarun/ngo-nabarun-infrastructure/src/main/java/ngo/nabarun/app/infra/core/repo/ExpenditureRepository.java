package ngo.nabarun.app.infra.core.repo;


import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.Expenditure;

@Repository
public interface ExpenditureRepository extends MongoRepository<Expenditure,String>{
	

	
	@Query("{deleted : true }")
	List<Expenditure> findAllDeletedExpenditure();
	

	

	
}
