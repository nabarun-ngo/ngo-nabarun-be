package ngo.nabarun.app.infra.core.repo;


import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.Request;


@Repository
public interface RequestRepository extends MongoRepository<Request,String>{
	
	List<Request> findByStatusIn(Set<String> status);
	
}
