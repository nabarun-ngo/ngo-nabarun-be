package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ngo.nabarun.app.infra.core.entity.ExpenseEntity;

public interface ExpenseRepository extends MongoRepository<ExpenseEntity,String>,QuerydslPredicateExecutor<ExpenseEntity>{
	
}
