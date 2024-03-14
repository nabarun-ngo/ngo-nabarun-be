package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;


public interface DocumentRefRepository extends MongoRepository<DocumentRefEntity,String>{
	
	List<DocumentRefEntity> findByDocumentRefIdAndDocumentType(String refId, String refType);
}
