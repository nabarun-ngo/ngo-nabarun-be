package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;

@Repository
public interface DocumentRefRepository extends MongoRepository<DocumentRefEntity,String>{
	
	List<DocumentRefEntity> findByDocumentRefIdAndDocumentType(String refId, String refType);
}
