package ngo.nabarun.app.infra.core.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.JobEntity;

public interface JobsRepository extends MongoRepository<JobEntity,String>{


}
