package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ngo.nabarun.app.infra.core.entity.ExpenseItemEntity;

public interface ExpenseItemRepository extends MongoRepository<ExpenseItemEntity,String>,QuerydslPredicateExecutor<ExpenseItemEntity>{

}
