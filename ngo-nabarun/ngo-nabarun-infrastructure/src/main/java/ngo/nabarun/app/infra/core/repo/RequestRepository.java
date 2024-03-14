package ngo.nabarun.app.infra.core.repo;


import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.WorkflowEntity;


@Repository
public interface RequestRepository extends MongoRepository<WorkflowEntity,String>{
	
	List<WorkflowEntity> findByStatusIn(Set<String> status);
	
}
