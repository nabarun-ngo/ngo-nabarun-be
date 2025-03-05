package ngo.nabarun.app.infra.core.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ngo.nabarun.app.infra.core.entity.JobEntity;

public interface JobsRepository extends MongoRepository<JobEntity,String>,QuerydslPredicateExecutor<JobEntity>{

}
