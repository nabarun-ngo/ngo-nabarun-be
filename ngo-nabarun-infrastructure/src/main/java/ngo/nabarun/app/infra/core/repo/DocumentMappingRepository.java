package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.DocumentMappingEntity;


public interface DocumentMappingRepository extends MongoRepository<DocumentMappingEntity,String>{
	
	List<DocumentMappingEntity> findByDocumentRefIdAndDocumentType(String refId, String refType);
	List<DocumentMappingEntity> findByDocumentId(String documentId);

}
