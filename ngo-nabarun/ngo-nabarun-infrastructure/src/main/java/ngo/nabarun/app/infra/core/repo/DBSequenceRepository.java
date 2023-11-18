package ngo.nabarun.app.infra.core.repo;


import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.DBSequenceEntity;

@Repository
public interface DBSequenceRepository extends MongoRepository<DBSequenceEntity,String>{
	
	Optional<DBSequenceEntity> findByName(String name); 
}
