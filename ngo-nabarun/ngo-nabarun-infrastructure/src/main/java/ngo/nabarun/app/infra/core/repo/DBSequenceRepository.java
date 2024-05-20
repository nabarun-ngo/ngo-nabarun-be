package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import ngo.nabarun.app.infra.core.entity.DBSequenceEntity;

public interface DBSequenceRepository extends MongoRepository<DBSequenceEntity,String>{
	
}
