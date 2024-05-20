package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ngo.nabarun.app.infra.core.entity.WorkListEntity;


public interface WorkListRepository extends MongoRepository<WorkListEntity,String>,QuerydslPredicateExecutor<WorkListEntity>{
	
	
}
