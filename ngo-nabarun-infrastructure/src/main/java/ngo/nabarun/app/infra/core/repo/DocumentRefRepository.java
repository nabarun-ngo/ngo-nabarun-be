package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;


public interface DocumentRefRepository extends MongoRepository<DocumentRefEntity,String>{
	
}
