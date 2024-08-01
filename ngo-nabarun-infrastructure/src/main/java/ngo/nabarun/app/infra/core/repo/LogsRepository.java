package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.LogsEntity;

public interface LogsRepository extends MongoRepository<LogsEntity,String>{

	List<LogsEntity> findByCorelationId(String corelationId);
	
}
